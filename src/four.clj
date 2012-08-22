(ns four
  (import [javax.swing JFrame]
          [java.awt Color Canvas Dimension])
  (:gen-class))

(def w 800)
(def h 600)
(def hz 50)

(defn frame [g w h s]
  (let [c (Color. (rand-int 0xffffff))]
    (doto g
      (.setColor c)
      (.fillRect 0 0 w h)
      (.setColor Color/WHITE)
      (.drawString "4k.clj" 20 30))
    (assoc s :color c)))

(defn timeline [] (repeat frame))

(defn draw
  ([c f s]
     (let [b (.getBufferStrategy c)
           g (.getDrawGraphics b)
           [w h] [(.getWidth c) (.getHeight c)]]
       (try
         (f g w h (update-in s [:frame] inc))
         (finally
          (.dispose g)
          (when-not (.contentsLost b)
            (.show b)))))))

(defn run [s f]
  (let [t (System/nanoTime)]
    (try
      (let [s (f s)
            d (- (System/nanoTime) t)
            l (/ 1000000000 hz)]
        (when (<  d l)
          (Thread/sleep (/ (- l d) 1000000)))
        (assoc s :duration d))
      (catch Exception e
        (println e)))))

(defn animate [d fs]
  (map #(partial d %) fs))

(defn canvas []
  (let [f (doto (JFrame.)
            (.setResizable false)
            (.setVisible true)
            .requestFocus
            (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE))
        c (Canvas.)]
    (doto (.getContentPane f)
      (.setPreferredSize (Dimension. w h))
      (.add c))
    (.pack f)
    (doto c
      (.setIgnoreRepaint true)
      (.createBufferStrategy 2))))

(defn start []
  (reductions run {:frame 0} (animate (partial draw (canvas)) (timeline))))

(defn -main [& args]
  (dorun (start)))
