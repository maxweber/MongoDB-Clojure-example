(ns com.flowfact.webshoptest
  (:use clojure.test
        somnium.congomongo))

(defn init-mongo [f] (do (mongo! :db "webshop") (f)))

(defn clear-customer-collection [f] (do (destroy! :customer {}) (f)))

(def customer-otto-normal {:id "1234"
                           :name "Otto Normal"
                           :city "Berlin"
                           :numberOfOrders 4
                           :bankData {
                              :accountNumber "9876543210"
                              :bankCode "30020011"
                              :accountHolder "Otto Normal"
                              }
                           })

(defn insert-otto-normal [] (insert! :customer customer-otto-normal))

(use-fixtures :once init-mongo)
(use-fixtures :each clear-customer-collection)

(deftest should-work-with-mongodb
  (insert! :customer {:id "4711"
                      :name "Max Mustermann"
                      :city "Cologne"
                      :numberOfOrders 3})
  (let [saved-id (:id (fetch-one :customer))]
    (is (= "4711" saved-id)))
)

(deftest should-save-and-find-customers
  (insert-otto-normal)
  (let [saved-customer (fetch-one :customer)
        saved-id (:id saved-customer)
        saved-bank-code (get-in saved-customer [:bankData :bankCode])]
    (is (= "1234" saved-id))
    (is (= "30020011" saved-bank-code))
  )
)

(deftest should-increment-the-number-of-orders-if-the-customer-place-an-order
  (insert-otto-normal)
  (update! :customer {:id "1234"} {:$inc {:numberOfOrders 1}})
  (let [saved-customer (fetch-one :customer)]
  (is (= "Otto Normal" (saved-customer :name)))
  (is (= 5 (saved-customer :numberOfOrders)))
  )
 )
