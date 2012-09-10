(ns breddit.statenvertaling-ripper
  (:use breddit.data))

(defn unchunk "See: http://stackoverflow.com/questions/3407876/how-do-i-avoid-clojures-chunking-behavior-for-lazy-seqs-that-i-want-to-short-ci"
  [s]
  (when (seq s)
    (lazy-seq
      (cons (first s)
            (unchunk (next s))))))

(defn get-chapter [book-code, chapter]
  [chapter (clj-http.client/get (str "http://www.statenvertaling.nu/bijbel.php?boek=" book-code "&hoofdstuk=" chapter) {:as :byte-array})])

(defn contains-link-to-next-chapter [chapter-vec]
  (not (nil? 
    (re-find #"Ga naar volgend hoofdstuk" (String. (:body (chapter-vec 1)) "ISO-8859-1")))))

(defn get-chapters-seq [book-code]
  (map (partial get-chapter book-code) (unchunk (range 1 99))))

(defn chapters-seq [book-code]
  (take-while 
    contains-link-to-next-chapter
    (get-chapters-seq book-code)))

(defn write-book-chapter-html [data chapter-vec]
  (do
    (println (:nl data) (chapter-vec 0))
    (with-open [test (clojure.java.io/output-stream (str "ripped/" (:nl data) " " (chapter-vec 0) ".html"))]
      (.write   test (:body (chapter-vec 1)) ))))

(defn get-book [book-code]
  (let [data (book-code->data book-code)]
    (doseq [chapter-vec (chapters-seq book-code)]
      (write-book-chapter-html data chapter-vec))))

(defn download-herziene-statenvertaling []
  (doseq [code book-codes] (get-book code)))
