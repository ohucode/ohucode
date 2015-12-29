(ns ohucode.password
  (:import [javax.crypto SecretKeyFactory]
           [javax.crypto.spec PBEKeySpec]
           [java.util Base64]
           [java.security SecureRandom]))

(defn encode-base64 [bytes]
  "일반 Base64 인코딩"
  (.encodeToString (Base64/getEncoder) bytes))

(defn encode-urlsafe-base64 [bytes]
  "URL-safe Base64 인코딩: RFC4648"
  (.encodeToString (.withoutPadding (Base64/getUrlEncoder)) bytes))

(defn random-bytes [size]
  "안전한 랜덤 바이트 생성. size 길이의 랜덤 바이트를 만들어 base64로 인코딩."
  (let [rng (SecureRandom.)
        bytes (byte-array size)]
    (.nextBytes rng bytes)
    bytes))

(defn random-int []
  (.nextInt (SecureRandom.)))

(defn random-digits [len]
  "최대 10진수로 len 자리수 만큼의 랜덤 숫자 생성."
  (rem (Math/abs (random-int))
       (.intValue (Math/pow 10 len))))

(defn random-digits-str [len]
  (format (str "%0" len "d")
          (random-digits len)))

(def generate-passcode (partial random-digits-str 6))

(defn random-digest []
  "32바이트 길이의 임의 urlsafe-base64 문자열 생성."
  (encode-urlsafe-base64 (random-bytes 24)))

(defn pbkdf2 [password salt iterations derived-bits]
  "PBKDF2 해쉬값을 base64로 인코딩한 문자열로 생성"
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
  (defn ohucode-password-digest [userid password]
    (password-digest password (saltfn userid)))

  (defn ohucode-valid-password? [userid password raw]
    (valid-password-digest? password (saltfn userid) raw)))
