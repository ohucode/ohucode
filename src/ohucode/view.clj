(ns ohucode.view
  (:require [net.cgrand.enlive-html :as h]
            [net.cgrand.reload :as reload]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))

(h/defsnippet navigation "templates/navigation.html"
  [:nav]
  [])

(h/defsnippet footer "templates/footer.html"
  [:footer]
  [])

(h/deftemplate layout "templates/layout.html"
  [{:keys [title main]}]
  [:title] (h/content title)
  [:nav] (h/substitute (navigation))
  [:main] (h/substitute (main))
  [:footer] (h/substitute (footer)))

(h/defsnippet dashboard "templates/dashboard.html"
  [:main]
  []
  )

(defn anti-forgery-field []
  (h/html [:input {:type "hidden" :value *anti-forgery-token*}]))

(defn intro-guest []
  (layout
   {:title "오후코드 첫화면"
    :main (h/snippet
           "templates/intro_guest.html"
           [:main] []
           [:#sign-up-form]
           (h/append (anti-forgery-field)))}))

;; TODO: make reloading affect only in dev mode
(reload/auto-reload *ns*)
