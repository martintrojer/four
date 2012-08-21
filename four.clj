(ns four
  (import [javax.swing JFrame]
          [java.awt Color Frame Canvas Dimension]))

(def width 800)
(def height 600)
(def hz 50)

(defn draw [g width height]
  (doto g
    (.setColor (Color. (rand-int 0xffffff)))
    (.fillRect 0 0 width height)
    (.setColor Color/WHITE)
    (.drawString "4k.clj" 20 30)))

(defn run [canvas]
  (loop [t (System/nanoTime)]
    (try
      (let [bs (.getBufferStrategy canvas)
            [width height] [(.getWidth canvas) (.getHeight canvas)]]
        (doto (.getDrawGraphics bs) (draw width height) .dispose)
        (.show bs)
        (let [delta (- (System/nanoTime) t)
              frame-length (/  1000000000 hz)]
          (when (<  delta frame-length)
            (Thread/sleep (/ frame-length 1000000)))))
      (catch Exception e (println e)))
    (when (.isShowing canvas)
      (recur (System/nanoTime)))))

(defn -main []
  (let [frame (doto (JFrame.)
                (.setResizable false)
                (.setVisible true)
                (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE))
        canvas (Canvas.)]
    (doto (.getContentPane frame)
      (.setPreferredSize (Dimension. width height))
      (.add canvas))
    (.pack frame)
    (doto canvas
      (.setIgnoreRepaint true)
      (.createBufferStrategy 2))
    (send-off (agent canvas) run)))
