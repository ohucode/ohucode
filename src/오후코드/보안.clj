(ns 오후코드.보안
  (:import [java.security KeyFactory KeyPairGenerator SecureRandom Signature]
           [java.security.spec PKCS8EncodedKeySpec X509EncodedKeySpec]
           java.util.Base64
           javax.crypto.SecretKeyFactory
           javax.crypto.spec.PBEKeySpec))

(defn encode-base64
  "일반 Base64 인코딩"
  [bytes]
  (.encodeToString (Base64/getEncoder) bytes))

(defn decode-base64
  "일반 Base64 디코딩"
  [bytes]
  (.decode (Base64/getDecoder) bytes))

(defn encode-urlsafe-base64
  "URL-safe Base64 인코딩: RFC4648, https://www.ietf.org/rfc/rfc4648.txt"
  [bytes]
  (.encodeToString (.withoutPadding (Base64/getUrlEncoder)) bytes))

(defn decode-urlsafe-base64
  "URL-safe Base64 디코딩: RFC4648, https://www.ietf.org/rfc/rfc4648.txt"
  [bytes]
  (.decode (Base64/getUrlDecoder) bytes))

(defn random-bytes
  "안전한 랜덤 바이트 생성. size 길이의 랜덤 바이트를 만들어 base64로 인코딩."
  [size]
  (let [rng (SecureRandom.)
        bytes (byte-array size)]
    (.nextBytes rng bytes)
    bytes))

(defn random-int []
  (.nextInt (SecureRandom.)))

(defn random-digits
  "최대 10진수로 len 자리수 만큼의 랜덤 숫자 생성."
  [len]
  (rem (Math/abs (random-int))
       (.intValue (Math/pow 10 len))))

(defn random-digits-str [len]
  (format (str "%0" len "d")
          (random-digits len)))

(def generate-passcode (partial random-digits-str 6))

(defn random-digest
  "32바이트 길이의 임의 urlsafe-base64 문자열 생성."
  []
  (encode-urlsafe-base64 (random-bytes 24)))

(defn pbkdf2
  "PBKDF2 해쉬값을 base64로 인코딩한 문자열로 생성"
  [password salt iterations derived-bits]
  (let [spec (PBEKeySpec. (.toCharArray password) (.getBytes salt) iterations derived-bits)
        factory (SecretKeyFactory/getInstance "PBKDF2WithHmacSHA1")]
    (->> (.generateSecret factory spec)
         (.getEncoded)
         (encode-base64))))

(defn password-digest [password salt]
  (pbkdf2 password salt 100000 160))

(defn valid-password-digest? [password salt raw]
  (= raw (password-digest password salt)))

(let [saltfn (partial str "ohucode/")]
  (defn 오후코드-비번해쉬 [userid password]
    (password-digest password (saltfn userid)))

  (defn 오후코드-유효비번? [userid password raw]
    (valid-password-digest? password (saltfn userid) raw)))

(defn 개인키 [파일명]
  (let [스펙 (-> (slurp 파일명 :encoding "ISO-8859-1")
                 (.getBytes "ISO-8859-1")
                 (PKCS8EncodedKeySpec.))]
    (-> (KeyFactory/getInstance "RSA")
        (.generatePrivate 스펙))))

(defn 공개키 [파일명]
  (let [스펙 (-> (slurp 파일명 :encoding "ISO-8859-1")
                 (.getBytes "ISO-8859-1")
                 (X509EncodedKeySpec.))]
    (-> (KeyFactory/getInstance "RSA")
        (.generatePublic 스펙))))

(defn 키쌍생성
  ([] (키쌍생성 2048))
  ([bitsize] (-> (doto (KeyPairGenerator/getInstance "RSA")
                   (.initialize bitsize))
                 (.genKeyPair))))

(defn 바이트-서명 [개인키 내용]
  ;; https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Signature
  (-> (doto (Signature/getInstance "SHA256withRSA")
        (.initSign 개인키)
        (.update 내용))
      (.sign)))

(defn 바이트-서명확인 [공개키 내용 서명]
  (-> (doto (Signature/getInstance "SHA256withRSA")
        (.initVerify 공개키)
        (.update 내용))
      (.verify 서명)))

(def ^{:dynamic true
       :doc "오후코드 사이트 공통으로 쓸 개인키"}
  *개인키* (개인키 "conf/auth.pk8"))

(def ^{:dynamic true
       :doc "오후코드 사이트 공통으로 쓸 공개키"}
  *공개키* (공개키 "conf/auth.pub"))

(defn 서명 [^String 내용]
  (-> *개인키*
      (바이트-서명  (.getBytes 내용))
      encode-urlsafe-base64))

(defn 서명확인 [^String 내용 ^String 서명]
  (let [내용 (.getBytes 내용)
        서명 (decode-urlsafe-base64 서명)]
    (바이트-서명확인 *공개키* 내용 서명)))

(defn 인증토큰생성
  "인증쿠키에 보관할 [인증문자열:서명] 값을 생성한다."
  [인증정보]
  (let [인증문자열 (-> 인증정보 pr-str .getBytes encode-urlsafe-base64)
        서명 (서명 인증문자열)]
    (str 인증문자열 ":" 서명)))

(defn 인증토큰확인
  "인증쿠키에 있는 인증문자열과 서명을 확인해서,
   서명이 유효하다면 인증문자열을 read-string으로 읽어서 돌려주고,
   무효라면 nil을 반환한다. 디코딩에 실패하더라도 nil이다."
  [토큰문자열]
  (try
    (let [[인증문자열 서명] (clojure.string/split 토큰문자열 #":")]
      (if (서명확인 인증문자열 서명)
        (-> 인증문자열 .getBytes decode-urlsafe-base64 String. read-string)))
    (catch Exception e nil)))
