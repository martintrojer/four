(ns vlead
  (require [clojure.java.io :as io])
  (import [javax.sound.sampled AudioSystem AudioFormat AudioFileFormat$Type AudioInputStream SourceDataLine]
          [java.io ByteArrayInputStream]))

;; In rememberance of http://www.nada.kth.se/~raberg/vl.html, my 1996-97 Nord Lead simulator.
;; It more or less worked, was written in C++.

;; It cannot do much, try:
;; (->> (chord [[:a 4] [:e 4] [:c# 5]] 1/8)
;;      (repeat 8)
;;      (map play))

;; I don't like the way the time / current frame pollutes the oscillator generation. Early days.

(def bpm 120)
(def sample-rate 44100)
(def audio-format (AudioFormat. sample-rate 16 1 true true))

(def latency 10)
(def buffer-size (/ (* sample-rate (.getFrameSize audio-format)) (/ 1000 latency)))

(def buffer (byte-array buffer-size))
(def ^SourceDataLine line-out (AudioSystem/getSourceDataLine audio-format))

(defn frequency [offset-from-a4]
  (* 440.0 (Math/pow 2 (/ offset-from-a4 12.0))))

(defn midi-note [midi]
  (frequency (- midi 69)))

(def notes [:a :a# :b :c :c# :d :d# :e :f :f# :g :g#])

(defn note [[n oct]]
  (frequency (+ (.indexOf notes n) (* 12 (- oct 4)))))

(defn period [^double hz]
  (/ sample-rate hz))

(defn phase [^double period ^long t]
  (/ (mod t period) period))

(defn sin [^double hz ^long t]
  (Math/sin (* (phase (period hz) t) 2.0 Math/PI)))

(defn sqr [^double hz ^long t]
  (if (< (phase (period hz) t) 0.5) 1.0 -1.0))

(defn tri [^double hz ^long t]
  (let [phase (phase (period hz) t)]
    (cond (< phase 0.25) (* phase 4)
          (< phase 0.75) (* (- phase 0.5) -4)
          :else (* (- phase 1) 4))))

(defn saw [^double hz ^long t]
  (let [phase (phase (period hz) t)]
    (if (< phase 0.5) (* phase 2) (* (- phase 1) 2))))

(defn rnd [_ _]
  (- (rand 2) 1))

(def seconds-per-beat (/ 60.0 bpm))

(defn tone
  ([] (tone [:a 4]))
  ([note] (tone note 1.0))
  ([note length] (tone note length sin))
  ([[n oct & [note-osc vol]] length osc]
     (let [length (* length seconds-per-beat sample-rate)]
       (->> (iterate inc 0)
            (map (partial (or note-osc osc) (note [n oct])))
            (map (partial * (or vol 1)))
            (take length)))))

(defn mix [& tracks]
  (/ (apply + tracks) (count tracks)))

(defn chord
  ([notes length] (chord notes length sin))
  ([notes length osc]
     (->> notes
          (map #(tone % length osc))
          (apply map mix))))

(defn clip [^double sample]
  (min 1.0 (max -1.0 sample)))

(def max-amplitude (dec (Math/pow 2 (dec (.getSampleSizeInBits audio-format)))))

(defn bits [^double sample]
  (Math/round (* sample max-amplitude)))

(defn write-sample-byte [^"[B" buffer ^long offset ^long sample]
  (let [high (unchecked-byte (+ (bit-and (bit-shift-right sample 8) 0xFF)))
        low (unchecked-byte (bit-and sample 0xFF))]
    (aset-byte buffer (int offset) high)
    (aset-byte buffer (int (inc offset)) low)
    buffer))

(defn write-sample-buffer [^"[B" buffer samples]
  (loop [i 0
         samples samples]
    (when samples
      (write-sample-byte buffer i (first samples))
      (recur (+ i 2) (next samples))))
  [buffer (* 2 (count samples))])

(defn out [[buffer available]]
  (.write line-out buffer 0 available))

(defn play [samples]
  (->> samples
       (map (comp bits clip))
       (partition (/ buffer-size 2) (/ buffer-size 2) (repeat 0))
       (map (comp out (partial write-sample-buffer buffer)))
       dorun))

(defn write
  ([samples] (write "test.wav" samples))
  ([file samples]
     (let [bytes (byte-array (* (count samples) 2))]
       (write-sample-buffer bytes (map (comp bits clip) samples))
       (AudioSystem/write (AudioInputStream. (ByteArrayInputStream. bytes) audio-format (count samples))
                          AudioFileFormat$Type/WAVE (io/file file)))))

(defn start []
  (.open line-out audio-format buffer-size)
  (.start line-out))

(defn stop []
  (.stop line-out)
  (.close line-out))

(start)