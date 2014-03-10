(ns tappedcv.core
  (:require [tappedcv.adb :as adb]
            [tappedcv.templates :as t]
            [tappedcv.settings :as s])
  (:gen-class))

(def screenshot-path (s/retrieve :host-screenshot-path))

(defn tap-dollarsigns-if-detected [results]
  (doseq [r results]
    (let [foundpoint (get r :match-point)]
      (println "Found dollar sign: " r)
      (adb/tap (.x foundpoint) (.y foundpoint)))))


(defn -main[] 
  (loop []
    (adb/save-screenshot screenshot-path)
    (let [results (t/find-dollarsign-locations (t/screenshot screenshot-path) t/preferred-match-method)]
      (tap-dollarsigns-if-detected (t/results-within-region results (s/retrieve :min-x) (s/retrieve :max-y)))
      (recur))))
