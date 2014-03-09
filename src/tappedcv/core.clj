(ns tappedcv.core
  (:require [tappedcv.adb :as adb]
            [tappedcv.templates :as t]
            [tappedcv.settings :as s])
  (:gen-class))

(def screenshot-path (s/retrieve :host-screenshot-path))

(defn tap-arrow-if-detected [results]
  (let [foundpoint (get results :match-point)]
    (if (>= (get results :metric) t/preferred-threshold)
      (do
        (println "Found arrow: " results)
        (adb/tap (.x foundpoint) (.y foundpoint))))))

(defn tap-dollarsign-if-detected [results]
  (let [foundpoint (get results :match-point)]
    (if (>= (get results :metric) t/preferred-threshold)
      (do
        (println "Found dollar sign: " results)
        (adb/tap (.x foundpoint) (.y foundpoint))))))

(defn tap-dollarsigns-if-detected [results]
  (doseq [r results]
    (let [foundpoint (get r :match-point)]
      (println "Found dollar sign: " r)
      (adb/tap (.x foundpoint) (.y foundpoint)))))


(defn -main[] 
  (loop []
    (adb/save-screenshot screenshot-path)
    (let [results (t/find-dollarsign-locations (t/screenshot screenshot-path) t/preferred-match-method)
          arrowresults (t/get-arrow (t/screenshot screenshot-path) t/preferred-match-method)] 
      (tap-arrow-if-detected arrowresults)
      (tap-dollarsigns-if-detected results)
      (recur))))
