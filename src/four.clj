;; https://github.com/yogthos/metaballs

(ns four
  (:import
   [javax.swing JFrame]
   [java.awt Canvas Graphics Color]
   java.awt.image.BufferStrategy)
  (:gen-class))

(set! *warn-on-reflection* true)
(def ^:const SIZE 250)

(defn direction [p v]
  (if (or (> p SIZE) (neg? p )) (- v) v))

(defn move [{:keys [x y vx vy radius color]}]
  (let [vx (direction x vx)
        vy (direction y vy)]
    {:x (+ x vx)
     :y (+ y vy)
     :vx vx
     :vy vy
     :radius radius
     :color color}))

(defn color-in-range [c]
  (if (< c 0) 0 (if (> c 255) 255 c)))

(defn color [r g b]
  (new Color 
       (int (color-in-range r)) 
       (int (color-in-range g)) 
       (int (color-in-range b))))

(defn influence [{:keys [x y radius]} px py]
  (let [dx (double (- x px))
        dy (double (- y py))]
    (double (/ radius (Math/sqrt (+ (* dx dx) (* dy dy)))))))

(defn compute-color [x y [red-cur green-cur blue-cur] ball]
  (let [influence (influence ball x y) 
        [r g b] (:color ball)]
    [(+ red-cur (* influence r))
     (+ green-cur (* influence g))
     (+ blue-cur (* influence b))]))

(defn paint-square [^Graphics g [red green blue] x y size]
  (doto g
      (.setColor ^Color (color red green blue))
      (.fillRect x y size size)))

(defn draw [^Canvas canvas balls]
  (let [buffer (.getBufferStrategy canvas)
        g      (.getDrawGraphics buffer)
        step   2]
    (try
      (loop [x 0]
        (loop [y 0]
          (paint-square g (reduce (partial compute-color x y) [0 0 0] balls) x y step)
          (if (< y (- SIZE step)) (recur (+ y step))))
        (if (< x (- SIZE step)) (recur (+ x step))))
      
      (finally (.dispose g)))
    (if-not (.contentsLost buffer)
      (.show buffer)) ))
 
(defn metaball []
  {:x (rand-int SIZE)
   :y (rand-int SIZE)
   :vx (double (inc (rand-int 6)))
   :vy (double (inc (rand-int 6)))
   :radius (+ 40 (rand-int 15))
   :color (take 3 (repeatedly #(rand-int 256)))})

(defn -main [& args]
  (let [frame (JFrame. "Metaballs")
        canvas (Canvas.)]
     
    (doto frame
      (.setSize SIZE SIZE)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setResizable false)
      (.add canvas)
      (.setVisible true))
 
    (doto canvas
      (.createBufferStrategy 2)
      (.setVisible true)
      (.requestFocus))
         
    (loop [balls (take 2 (repeatedly metaball))]
      (draw canvas balls)
      (recur (map move balls)))))
