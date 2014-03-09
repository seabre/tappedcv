(ns tappedcv.templates
 (:import [org.opencv.core Mat Size CvType Core Point]
          [org.opencv.highgui Highgui]
          [org.opencv.imgproc Imgproc]))

(clojure.lang.RT/loadLibrary Core/NATIVE_LIBRARY_NAME)

;(def dollarsign (Highgui/imread "resources/images/dollarsign.png"))
(def arrow (Highgui/imread "/home/seabre/arrow.png" 0))
(def dollarsign (Highgui/imread "/home/seabre/dollarsign.png" 0))

(def preferred-match-method (Imgproc/TM_CCOEFF_NORMED))
(def preferred-threshold 0.7)

(def max-x 160)
(def max-y 940)

; Icons move, so increase height by this much.
; 200 is a good value, but you may need to tweak this.
(def height-buffer 175)

(defn screenshot [imgpath] 
  (let [img (Highgui/imread imgpath)]
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

(defn find-results [image template match-method]
  (let [resultmatrix (match-template image template match-method)]
    (vec-within-threshold (mat-as-vec resultmatrix template) preferred-threshold)))

(defn find-dollarsign-results [image match-method]
  (find-results image dollarsign match-method))

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

(defn get-arrow [image match-method]
  (let [result (match-location image arrow match-method)
        newx (+ (.x (get result :match-point)) 20)
        newy (+ (.y (get result :match-point)) 20)]
     (match-result (Point. newx newy) (get result :metric))))
