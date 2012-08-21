(ns four
  (import [javax.swing JFrame]
          [java.awt Color Canvas Dimension])
  (:gen-class))

(def w 800)
(def h 600)
(def hz 50)

(defn frame [g w h]
  (doto g
    (.setColor (Color. (rand-int 0xffffff)))
    (.fillRect 0 0 w h)
    (.setColor Color/WHITE)
    (.drawString "4k.clj" 20 30)))

(defn draw [c]
  (let [b (.getBufferStrategy c)
        [w h] [(.getWidth c) (.getHeight c)]
        g (.getDrawGraphics b)]
    (try
      (frame g w h)
      (finally
       (.dispose g)))
    (when-not (.contentsLost b)
      (.show b))))

(defn run [c]
  (loop [t (System/nanoTime)]
    (try
      (draw c)
      (let [d (- (System/nanoTime) t)
            l (/ 1000000000 hz)]
        (when (<  d l)
          (Thread/sleep (/ l 1000000))))
      (catch Exception e
        (println e)))
    (when (.isShowing c)
      (recur (System/nanoTime)))))

(defn -main [& args]
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
    (send-off (agent c) run)))
