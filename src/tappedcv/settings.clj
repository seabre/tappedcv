(ns tappedcv.settings
  (:require [clojure.edn :as edn]))

(def settings-file-name ".tappedcv")

(def user-home-dir (System/getProperty "user.home"))

(def settings-location (str user-home-dir "/" settings-file-name))

(def settings (edn/read-string (slurp settings-location)))

(defn retrieve [key]
  (get settings key))

