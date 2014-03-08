(ns tappedcv.adb
  (:require [me.raynes.conch :refer [programs with-programs let-programs]]
            [clojure.string :as string]))

(def android-screenshot-path "/sdcard/tappedcv-screenshot.png")

(defn tap [x y]
  (let [intx (str (int x))
        inty (str (int y))]
  (with-programs [adb]
    (adb "shell" "input" "tap" intx inty))))

(defn save-screenshot [filepath]
  (with-programs [adb mogrify]
    (adb "shell" "/system/bin/screencap" "-p" android-screenshot-path)
    (adb "pull" android-screenshot-path filepath)
    ; Probably should make this happen in OpenCV instead.
    (mogrify "-rotate" "270" filepath)))
