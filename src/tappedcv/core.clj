(ns tappedcv.core
  (:require [tappedcv.adb :as adb]
            [tappedcv.templates :as t])
  (:gen-class))

(def screenshot-path "/tmp/tappedcv-screenshot.png")

(defn tap-arrow-if-detected [results]
  (let [foundpoint (get results :match-point)]
    (if (>= (get results :metric) 0.7)
      (do
        (println "Found arrow: " results)
        (adb/tap (.x foundpoint) (.y foundpoint))))))

(defn tap-dollarsign-if-detected [results]
  (let [foundpoint (get results :match-point)]
    (if (>= (get results :metric) 0.7)
      (do
        (println "Found dollar sign: " results)
        (adb/tap (.x foundpoint) (.y foundpoint))))))


(defn -main[] 
  (loop []
    (adb/save-screenshot screenshot-path)
    (let [results (t/get-center-dollarsign (t/screenshot screenshot-path) t/preferred-match-method)
          arrowresults (t/get-arrow (t/screenshot screenshot-path) t/preferred-match-method)] 
      (tap-arrow-if-detected arrowresults)
      (tap-dollarsign-if-detected results)
      (recur))))
