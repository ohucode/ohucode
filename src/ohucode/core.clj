(ns ohucode.core)

(def ^:dynamic
  ^{:doc "클라이언트 IP"}
  *client-ip* "0.0.0.0")

(def ^:dynamic
  ^{:doc "로그인한 사용자 정보"}
  *signed-user*)

(def brand-name "오후코드")

(defn brand-name+ [& strs]
  (apply str (concat brand-name " " strs)))

(def ^:dynamic
  ^{:doc "가입 인증코드 발급후 유효시간 (단위: 초)"}
  *passcode-expire-sec* (* 30 60))

(defn session-user [req]
  (get-in req [:session :user]))

(def signed-in? (comp not nil? session-user))

(defn wrap-user-info [handler]
  (fn [req]
    (binding [*signed-user* (session-user req)]
      (handler req))))
