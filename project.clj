(defproject tappedcv "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [opencv/opencv "2.4.8"]
                 [opencv/opencv-native "2.4.8"]
                 [me.raynes/conch "0.5.0"]]
  :main ^:skip-aot tappedcv.core
  ;:global-vars {*warn-on-reflection* true}
  :profiles {:uberjar {:aot :all} :dev {:dependencies [[midje "1.6.2"]] :resource-paths ["test/testresources"]}}
  :plugins [[lein-localrepo "0.5.3"] [lein-midje "3.1.3"]]
  :injections [(clojure.lang.RT/loadLibrary org.opencv.core.Core/NATIVE_LIBRARY_NAME)])
