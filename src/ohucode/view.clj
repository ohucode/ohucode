(ns ohucode.view
  (:require [net.cgrand.enlive-html :as h]
            [net.cgrand.reload :as reload]))

(h/defsnippet navigation "templates/navigation.html"
  [:nav]
  [])

(h/defsnippet footer "templates/footer.html"
  [:footer]
  [])

(h/deftemplate layout "templates/layout.html"
  [{:keys [title main]}]
  [:title] (html/content title)
  [:nav] (html/substitute (navigation))
  [:main] (html/substitute main)
  [:footer] (html/substitute (footer)))

