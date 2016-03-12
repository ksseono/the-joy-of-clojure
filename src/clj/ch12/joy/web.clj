;;
;; Listing 12.1
;;
(ns joy.web
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [com.sun.net.httpserver HttpHandler HttpExchange HttpServer]
           [java.net InetSocketAddress URLDecoder URI]
           [java.io File FilterOutputStream]))

(def OK java.net.HttpURLConnection/HTTP_OK)

(defn respond
  ([exchange body]
   (respond identity exchange body))
  ([around exchange body]
   (.sendResponseHeaders exchange OK 0)
   (with-open [resp (around (.getResponseBody exchange))]
     (.write resp (.getBytes body)))))


;;
;; Listing 12.2
;;
(defn new-server [port path handler]
  (doto
      (HttpServer/create (InetSocketAddress. port) 0)
    (.createContext path handler)
    (.setExecutor nil)
    (.start)))


;;
;; Listing 12.3
;;
(defn default-handler [txt]
  (proxy [HttpHandler]
      []
    (handle [exchange]
      (respond exchange txt))))

(comment
  (def server
    (new-server
      8123
      "/joy/hello"
      (default-handler "Hello Cleveland")))
  (.stop server 0)
  )

(def p (default-handler
         "There's no problem that can't be solved with another level of indirection"))

(comment
  (def server (new-server 8123 "/" p))

  (update-proxy p
  {"handle" (fn [this exchange]
              (respond exchange (str "this is " this)))})
  )

;;
;; Listing 12.4
;;
(def echo-handler
  (fn [_ exchange]
    (let [headers (.getRequestHeaders exchange)]
      (respond exchange (prn-str headers)))))

(comment
  (update-proxy p {"handle" echo-handler})
  )

;;
;; Listing 12.5
;;
(defn html-around [o]
  (proxy [FilterOutputStream]
      [o]
    (write [raw-bytes]
      (proxy-super write
        (.getBytes (str "<html><body>"
                     (String. raw-bytes)
                     "</body></html>"))))))

(defn listing [file]
  (-> file .list sort))

(comment
  (listing (io/file "."))
  ;;=> (".gitignore" "README.md" "project.clj" "src" "target" "test")

  (listing (io/file "./README.md"))
  ;;=> ()
  )

;;
;; Listing 12.6
;;
(defn html-links [root filenames]
  (string/join
    (for [file filenames]
      (str "<a href='"
        (str root
          (if (= "/" root)
            ""
            File/separator)
          file)
        "'>"
        file "</a><br>"))))

(comment
  (html-links "." (listing (io/file ".")))
  ;;=> "<a href='./.gitignore'>.gitignore</a><br>
  ;;    <a href='./README.md'>README.md</a><br>
  ;;    <a href='./project.clj'>project.clj</a><br>
  ;;    <a href='./src'>src</a><br>
  ;;    <a href='./target'>target</a><br>
  ;;    <a href='./test'>test</a><br>"
  )

;;
;; Listing 12.7
;;
(defn details [file]
  (str (.getName file) " is "
    (.length file) "bytes."))

(comment
  (details (io/file "./README.md"))
  ;;=> README.md is 401bytes.
  )

;;
;; Listing 12.8
;;
(defn uri->file [root uri]
  (->> uri
    str
    URLDecoder/decode
    (str root)
    io/file))

(comment
  (uri->file "." (URI. "/project.clj"))
  ;;=> #object[java.io.File 0x4e00badd "./project.clj"]
  
  (details (uri->file "." (URI. "/project.clj")))
  ;;=> project.clj is 1305bytes.
  )

;;
;; Listing 12.9
;;
(def fs-handler
  (fn [_ exchange]
    (let [uri (.getRequestURI exchange)
          file (uri->file "." uri)]
      (if (.isDirectory file)
        (do (.add (.getResponseHeaders exchange)
              "Content-Type" "text/html")
            (respond html-around
              exchange
              (html-links (str uri) (listing file))))
        (respond exchange (details file))))))

(comment
  (update-proxy p {"handle" fs-handler})
  )
