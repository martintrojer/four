(ns four
  (import [javax.swing JFrame]
          [java.awt Color Canvas Dimension])
  (:gen-class))

(def w 800)
(def h 600)
(def hz 50)

(defn frame [g]
  (doto g
    (.setColor (Color. (rand-int 0xffffff)))
    (.fillRect 0 0 w h)
    (.setColor Color/WHITE)
    (.drawString "4k.clj" 20 30)))

(defn draw [f c]
  (let [b (.getBufferStrategy c)
        g (.getDrawGraphics b)]
    (try
      (f g)
      (finally
       (.dispose g)))
    (when-not (.contentsLost b)
      (.show b))))

(defn run [f c]
  (let [t (System/nanoTime)]
    (try
      (let [_ (f c)
            d (- (System/nanoTime) t)
            l (/ 1000000000 hz)]
        (when (<  d l)
          (Thread/sleep (/ (- l d) 1000000)))
        {:duration d})
      (catch Exception e
        (println e)))))

(defn start []
  (let [f (doto (JFrame.)
            (.setResizable false)
            (.setVisible true)
            (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE))
        c (Canvas.)]
    (doto (.getContentPane f)
      (.setPreferredSize (Dimension. w h))
      (.add c))
    (.pack f)
    (doto c
      (.setIgnoreRepaint true)
      (.createBufferStrategy 2))
    (repeatedly #(run (partial draw frame) c))))

(defn -main [& args]
  (dorun (start)))
