(ns tappedcv.templates
 (:import [org.opencv.core Mat Size CvType Core Point]
          [org.opencv.highgui Highgui]
          [org.opencv.imgproc Imgproc])
  (:require [tappedcv.settings :as s]))

(clojure.lang.RT/loadLibrary Core/NATIVE_LIBRARY_NAME)

(def dollarsign (Highgui/imread (s/retrieve :dollarsign) 0))

(def preferred-match-method (Imgproc/TM_CCOEFF_NORMED))
(def preferred-threshold (s/retrieve :threshold))

; Icons move, so increase height by this much.
; 200 is a good value, but you may need to tweak this.
(def height-buffer (s/retrieve :height-buffer))

(defn rotate-image-counterclockwise [image]
  (let [imageresult image]
    (Core/transpose image imageresult)
    (Core/flip imageresult, imageresult, 0)
    imageresult))

(defn screenshot [imgpath] 
  ; For some reason, we always get back the screenshot from adb
  ; in portrait mode. We need to rotate the image counter-clockwise
  (let [img (rotate-image-counterclockwise (Highgui/imread imgpath))]
    (Imgproc/cvtColor img img Imgproc/COLOR_BGR2GRAY)
    img))

(defn result-rows [image template]
  (+ (- (.rows image) (.rows template)) 1))

(defn result-cols [image template]
  (+ (- (.cols image) (.cols template)) 1))

(defn result-matrix [rows cols]
  (Mat. rows cols CvType/CV_32FC1))

(defn match-result [matchpoint metric]
  {:match-point matchpoint :metric metric})

(defn match-point-center [x y template heightbuffer]
  (Point. (+ x (/ (.cols template) 2)) (+ y (/ (.rows template) 2) heightbuffer)))

(defn match-template [image template match-method]
  (let [result image]
    (Imgproc/matchTemplate image template result  match-method)
    (Core/pow result 5 result)
    result))

(defn row-to-results [resultmatrix template row]
  (let [resultrow (.row resultmatrix row)
       resultrowarr (float-array (.cols resultrow))]
    (.get resultrow 0 0 resultrowarr)
    (map-indexed (fn [i j] (match-result (match-point-center i row template height-buffer) j)) (vec resultrowarr))))

(defn mat-as-vec [resultmatrix template]
  (pmap (fn [i] (row-to-results resultmatrix template i)) (range 0 (.rows resultmatrix))))

(defn vec-within-threshold [v threshold]
  (flatten (pmap (fn [i] (filter #(>= (get % :metric) threshold) i)) v)))

(defn match-locations [image template match-method]
  (let [resultmatrix (match-template image template match-method)]
    (vec-within-threshold (mat-as-vec resultmatrix template) preferred-threshold)))

(defn find-dollarsign-locations [image match-method]
  (match-locations image dollarsign match-method))

(defn match-location [image template match-method]
  (let [result (match-template image template match-method)
        minmaxresult (Core/minMaxLoc result)]
    (if (or (= match-method Imgproc/TM_SQDIFF) (= match-method Imgproc/TM_SQDIFF_NORMED))
      (match-result (.minLoc minmaxresult) (.minVal minmaxresult))
      (match-result (.maxLoc minmaxresult) (.maxVal minmaxresult)))))

(defn get-center [image template match-method]
  (let [matchloc (match-location image template match-method)
        point (get matchloc :match-point)
        metric (get matchloc :metric)]
    (match-result (match-point-center (.x point) (.y point) template height-buffer) metric)))

(defn get-center-dollarsign [image match-method]
  (get-center image dollarsign match-method))
