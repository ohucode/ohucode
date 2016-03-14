(ns 오후코드.password
  (:use [미생.기본])
  (:import [javax.crypto SecretKeyFactory]
           [javax.crypto.spec PBEKeySpec]
           [java.util Base64]
           [java.security SecureRandom KeyFactory Signature]
           [java.security.spec PKCS8EncodedKeySpec X509EncodedKeySpec]))

(함수 encode-base64 [bytes]
  "일반 Base64 인코딩"
  (.encodeToString (Base64/getEncoder) bytes))

(함수 decode-base64 [bytes]
  "일반 Base64 디코딩"
  (.decode (Base64/getDecoder) bytes))

(함수 encode-urlsafe-base64 [bytes]
  "URL-safe Base64 인코딩: RFC4648"
  (.encodeToString (.withoutPadding (Base64/getUrlEncoder)) bytes))

(함수 random-bytes [size]
  "안전한 랜덤 바이트 생성. size 길이의 랜덤 바이트를 만들어 base64로 인코딩."
  (가정 [rng (SecureRandom.)
         bytes (byte-array size)]
    (.nextBytes rng bytes)
    bytes))

(함수 random-int []
  (.nextInt (SecureRandom.)))

(함수 random-digits [len]
  "최대 10진수로 len 자리수 만큼의 랜덤 숫자 생성."
  (rem (Math/abs (random-int))
       (.intValue (Math/pow 10 len))))

(함수 random-digits-str [len]
  (format (str "%0" len "d")
          (random-digits len)))

(정의 generate-passcode (partial random-digits-str 6))

(함수 random-digest []
  "32바이트 길이의 임의 urlsafe-base64 문자열 생성."
  (encode-urlsafe-base64 (random-bytes 24)))

(함수 pbkdf2 [password salt iterations derived-bits]
  "PBKDF2 해쉬값을 base64로 인코딩한 문자열로 생성"
  (가정 [spec (PBEKeySpec. (.toCharArray password) (.getBytes salt) iterations derived-bits)
         factory (SecretKeyFactory/getInstance "PBKDF2WithHmacSHA1")]
    (->> (.generateSecret factory spec)
         (.getEncoded)
         (encode-base64))))

(함수 password-digest [password salt]
  (pbkdf2 password salt 100000 160))

(함수 valid-password-digest? [password salt raw]
  (= raw (password-digest password salt)))

(가정 [saltfn (partial str "ohucode/")]
  (함수 ohucode-password-digest [userid password]
    (password-digest password (saltfn userid)))

  (함수 ohucode-valid-password? [userid password raw]
    (valid-password-digest? password (saltfn userid) raw)))

(함수 개인키 [파일명]
  (가정 [스펙 (-> (slurp 파일명 :encoding "ISO-8859-1")
                  (.getBytes "ISO-8859-1")
                  (PKCS8EncodedKeySpec.))]
    (-> (KeyFactory/getInstance "RSA")
        (.generatePrivate 스펙))))

(함수 공개키 [파일명]
  (가정 [스펙 (-> (slurp 파일명 :encoding "ISO-8859-1")
                  (.getBytes "ISO-8859-1")
                  (X509EncodedKeySpec.))]
    (-> (KeyFactory/getInstance "RSA")
        (.generatePublic 스펙))))

(함수 바이트-서명 [개인키 내용]
  ;; https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Signature
  (-> (doto (Signature/getInstance "SHA256withRSA")
         (.initSign 개인키)
         (.update 내용))
      (.sign)))

(함수 바이트-서명확인 [공개키 내용 서명]
  (-> (doto (Signature/getInstance "SHA256withRSA")
        (.initVerify 공개키)
        (.update 내용))
      (.verify 서명)))

(함수 서명 [^String 내용]
  (-> (개인키 "conf/auth.pk8")
      (바이트-서명  (.getBytes 내용))
      encode-base64))

(함수 서명확인 [^String 내용 ^String 서명]
  (가정 [키 (공개키 "conf/auth.pub.der")
         내용 (.getBytes 내용)
         서명 (decode-base64 서명)]
    (바이트-서명확인 키 내용 서명)))
