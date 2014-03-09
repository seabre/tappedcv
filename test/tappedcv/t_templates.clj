(ns tappedcv.t-templates
    (:use midje.sweet)
    (:require [tappedcv.templates :as t])
    (:import [org.opencv.highgui Highgui]
             [org.opencv.core Mat Core Point]))

; Load images at grayscale
(def screenshot (Highgui/imread (.getFile (clojure.java.io/resource "screenshot.png")) 0))
(def ccw-screenshot (Highgui/imread (.getFile (clojure.java.io/resource "counterclockwisescreenshot.png")) 0))
(def ccw-screenshot2 (Highgui/imread (.getFile (clojure.java.io/resource "counterclockwisescreenshot.png")) 0))
(def template (Highgui/imread (.getFile (clojure.java.io/resource "template.png")) 0))
; Pre-load result matrix. Mutates first argument. Create another dummy.
(def result-matrix (t/match-template ccw-screenshot2 template t/preferred-match-method))

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

(facts "match-template"
  (fact "It returns a result matrix"
    (-> (t/match-template ccw-screenshot template t/preferred-match-method) .getClass .getName) => "org.opencv.core.Mat"))

(facts "row-to-results"
  (fact "the processed row has as many columns as the source image"
    (count (t/row-to-results result-matrix template 0)) => (.cols ccw-screenshot2))
  (fact "each result in the processed row is a map with a match-point"
    (reduce #(and %1 %2) 
            (map #(contains? % :match-point) 
                 (t/row-to-results result-matrix template 0))) => true)
  (fact "each result in the processed row is a map with a metric"
    (reduce #(and %1 %2) 
            (map #(contains? % :metric) 
                 (t/row-to-results result-matrix template 0))) => true))

(facts "mat-as-vec"
  (fact "output has as many rows as the source image"
    (count (t/mat-as-vec result-matrix template)) => (.rows ccw-screenshot2)))

(facts "vec-within-threshold"
  (fact "returns results within threshold"
    (reduce #(and %1 %2) 
      (map #(>= (get % :metric) t/preferred-threshold) 
           (t/vec-within-threshold (t/mat-as-vec result-matrix template) t/preferred-threshold))) => true))

(facts "result-within-region"
  (fact "it returns false if within minimum x"
    (t/result-within-region (t/match-result (Point. 2 2) 0.1) 10 200) => false)
  (fact "it returns true if greater than minimum x"
    (t/result-within-region (t/match-result (Point. 11 2) 0.1) 10 200) => true)
  (fact "it returns false if equal to than minimum x"
    (t/result-within-region (t/match-result (Point. 10 2) 0.1) 10 200) => false)
  (fact "it returns false if within maximum y"
    (t/result-within-region (t/match-result (Point. 10 201) 0.1) 10 200) => false)
  (fact "it returns true if less than maximum y"
    (t/result-within-region (t/match-result (Point. 11 199) 0.1) 10 200) => true)
  (fact "it returns false if equal to than maximum y"
    (t/result-within-region (t/match-result (Point. 11 200) 0.1) 10 200) => false)
  (fact "it returns false if point is within minimum x and within maximum y"
    (t/result-within-region (t/match-result (Point. 2 201) 0.1) 10 200) => false)
  (fact "it returns true if point is not within minimum x and not within maximum y"
    (t/result-within-region (t/match-result (Point. 11 199) 0.1) 10 200) => true))

(facts "results-within-region"
  (fact "returns all results within region"
    (count (t/results-within-region [(t/match-result (Point. 10 199) 0.1) (t/match-result (Point. 11 199) 0.1)] 10 200)) => 1))
