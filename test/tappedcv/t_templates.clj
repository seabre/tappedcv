(ns tappedcv.t-templates
    (:use midje.sweet)
    (:require [tappedcv.templates :as t])
    (:import [org.opencv.highgui Highgui]
             [org.opencv.core Mat Core Point]))

; Load images at grayscale
(def screenshot (Highgui/imread (.getFile (clojure.java.io/resource "screenshot.png")) 0))
(def ccw-screenshot (Highgui/imread (.getFile (clojure.java.io/resource "counterclockwisescreenshot.png")) 0))
(def template (Highgui/imread (.getFile (clojure.java.io/resource "template.png")) 0))

(facts "rotate-image-counterclockwise"
  (fact "it rotates an image counterclockwise"
        (let [diff (Mat.)
              rotated (t/rotate-image-counterclockwise screenshot)]
          (Core/compare rotated ccw-screenshot diff Core/CMP_NE)
          (= (Core/countNonZero diff) 0)) => true))

(facts "match-result"
 (fact "it returns a map with a key that contains the point"
   (get (t/match-result (Point. 2 2) 0.2) :match-point) => (Point. 2 2))
 (fact "it returns a map with a key that contains the metric"
   (get (t/match-result (Point. 2 2) 0.2) :metric) => 0.2))

(facts "match-point-center"
  (fact "It returns the center point of the found template rectangle"
    (t/match-point-center 0 0 template 0) => (Point. 64.5 75))
  (fact "It returns the center point of the found template rectangle and increases Y coordinate by 10 if specified in heightbuffer"
    (t/match-point-center 0 0 template 10) => (Point. 64.5 85)))
