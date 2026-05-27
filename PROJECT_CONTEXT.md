Project goal
    Build a mini intelligent observability system that can 
    -Monitor system behavior in real time
    -Detect anomalies automatically

Architecture
    1.First we have a basic CRUD APIs which simulate an online marketplace. This is done with the help of controller, model and some parts of service package

    2. Monitoring package is responsible measuring metrics and putting them in a neat manner. These metrics are sent to the ML model through metricsController in the controller package.

    3. Errors are simulated through various packages. It is explained in more detail below

# Understanding How Errors Work in the Project

This document provides a simple, beginner-friendly explanation of how our system intentionally breaks itself. To test how reliable our application is, we have written special code that acts like a "gremlin" or a prankster—it secretly causes crashes, slows things down, or messes up data on purpose. All of this happens inside the `service` folder.

---

## Part 1: The Mastermind Behind the Chaos (InternalErrorSimulator)

Think of the `InternalErrorSimulator` as the control room for chaos. Every time a user tries to do something—like viewing a product or adding an item to their cart—the system pauses for a split second and asks the control room: *"Should I let this work normally, or should I mess it up?"*

### How It Works Step-by-Step:
1. **The Rulebook (Registration):** When the application starts, the `InternalErrorSimulator` writes down a rulebook for every possible action. For example, it writes down: *"If someone fetches products, we can either delay the response, crash the endpoint entirely, or give them bad data."*
2. **Checking In (Context Creation):** Whenever a piece of code (like the product service) is about to do its job, it creates a blank sticky note (called a `Context`). This sticky note says *"Hey, I am currently trying to run PRODUCT_FETCH."* 
3. **Rolling the Dice (Injection):** The service passes this sticky note to the `InternalErrorSimulator`. The simulator looks at its rulebook, sees the rules for `PRODUCT_FETCH`, and literally rolls a digital dice (checking a random percentage, like a 30% chance). 
4. **The Prank (Execution):** If the dice lands on a failure, the simulator writes instructions on that sticky note. It might write, `"Make the product list duplicate itself."` or it might just pull the plug and crash the app right then and there with a "System Down" error.
5. **The Aftermath:** The service reads the sticky note. If it sees instructions to mess up, it follows them! Instead of sending you 10 products, it might send you 20 by repeating the same 10 products twice.

### A Simple Real-World Example:
Imagine you open the app to look at the list of products (`productService.getList`).
- Your app asks the database for products. 
- Before showing them to you, the code hands a sticky note to the `InternalErrorSimulator`.
- The simulator rolls a 3-sided dice. It lands on "Missing Data". It writes `missingProducts = true` on the sticky note.
- The product code reads the note. Instead of giving you the full 150 products, it intentionally chops the list in half and only shows you 75 products. You, the user, think the other 75 products disappeared!

---

## Part 2: How Every Single Feature Can Break (Method-by-Method)

Every single feature in our app relies on two things: **External Dependencies** (like databases or payment gateways) and our **Internal Errors** (the sticky note pranks). Here is exactly how every feature can break.

### **1. Product Features (`productService`)**

* **Adding a New Product (`addProduct`)**
  * **What it talks to:** It tries to talk to a fake database (`fakeDBClient`).
  * **How the Database fails:** The database might take too long to respond (Timeout), or it might refuse the connection entirely.
  * **How our System fails:** The simulator might just throw a massive "System Down" error, stopping you from adding the product completely.
  * **Example Scenario:** You click "Add Product". The system tries to save it, but the database connection acts like a slow internet connection. After waiting too long, it gives up and shows you an error screen.

* **Viewing Products (`getList`)**
  * **What it talks to:** The database to get the products, and a third-party API to get ratings and recommendations.
  * **How our System fails:** This one is heavily pranked! It can do three things:
    1. **Missing Data:** It throws away half the products.
    2. **Duplicate Data:** It copies the products, showing you everything twice.
    3. **Stale Data:** It intentionally lowers the price of every product by $10 to make it look like the prices haven't been updated properly.

* **Updating a Product (`updateProd`)**
  * **What it talks to:** The database.
  * **How our System fails:** 
    1. **Update Ignored:** You change the name of a product. The system says "Success!" but secretly ignores your change.
    2. **Partial Update:** You try to change the name and the price. It only changes the name and forgets the price.
    3. **Wrong Price:** You try to change the price to $50. The simulator maliciously adds $20 to it, saving the price as $70 instead.

* **Deleting a Product (`delProd`)**
  * **How our System fails:** The simulator rolls the dice and simply crashes the process midway through. You click delete, and the app just breaks with an `OPERATION_FAILED` error.

---

### **2. Shopping Cart Features (`cartService`)**

* **Viewing the Cart (`getCart`)**
  * **How our System fails:** Just like the product list, it can either hide half the items in your cart so you panic thinking they are gone, or it can duplicate everything so it looks like you are buying two of everything.

* **Adding to Cart (`addToCart`)**
  * **What it talks to:** The database to save the cart, and an external API to check inventory.
  * **How our System fails:** 
    1. **Product Deleted Fakeout:** You click "Add to Cart". The system lies and says, *"Sorry, this product is suddenly unavailable"* even though it actually is available.
    2. **Wrong Quantity:** You add exactly 1 laptop to your cart. The simulator steps in and quietly changes the quantity to 2.

* **Removing from Cart (`delCartItem`)**
  * **How our System fails:** Random crashes. You try to remove an item, and the system just gives up and throws a "System Down" error.

---

### **3. Order Features (`orderService`)**

* **Creating an Order (`createOrder`)**
  * **What it talks to:** The database, an API for delivery estimates, and a payment gateway to pre-check your card.
  * **How our System fails:** It creates an **Empty Order**. 
  * **Example Scenario:** You have 5 items in your cart. You hit checkout. The simulator takes over, wipes your cart clean, creates an order with 0 items, and charges you $0.00. You get an order confirmation for absolutely nothing.

* **Viewing your Orders (`getOrders`)**
  * **How our System fails:** 
    1. **Missing/Duplicate Orders:** Hides some of your past orders or shows them twice.
    2. **Wrong Status:** You just placed an order, so the status should be "Pending". The simulator forcefully changes the text to say "DELIVERED", confusing the customer.

* **Deleting/Canceling an Order (`deleteOrder`)**
  * **What it talks to:** The payment gateway to process a refund.
  * **How our System fails:** The payment gateway might refuse to process the refund, or the system might crash entirely, leaving the order stuck in limbo.

---

### **4. Payment Features (`PaymentService`)**

* **Processing a Payment (`processPayment`)**
  * **What it talks to:** A fraud check API, the payment gateway, and the database.
  * **How our System fails:** 
    1. **Success but Not Saved:** Your credit card is charged, the payment is successful, but the system "forgets" to save the receipt in the database.
    2. **Double Payment:** The system accidentally charges you twice and creates two payment records.
    3. **Status Mismatch:** The payment is 100% successful, but the system updates your order status to `FAILED`. The customer thinks their payment failed, even though it didn't!

---

## Part 3: Quick Summary Cheat Sheet

Here is a quick breakdown of exactly what file and method causes what specific bug:

* **`productService.java`**
  * **`addProduct`** ➔ Database Timeout / Entire System Crashes
  * **`getList`** ➔ Shows half the products / Shows double the products / Shows outdated, wrong prices
  * **`updateProd`** ➔ Pretends to update but doesn't / Only updates half the info / Maliciously alters the price you typed
  * **`delProd`** ➔ Randomly crashes when you click delete
* **`cartService.java`**
  * **`getCart`** ➔ Hides items in your cart / Duplicates items in your cart
  * **`addToCart`** ➔ Lies that a product is unavailable / Adds 1 extra quantity to what you requested
  * **`delCartItem`** ➔ Randomly crashes when you click remove
* **`orderService.java`**
  * **`createOrder`** ➔ Creates a completely blank order with $0 total / External APIs fail to respond
  * **`getOrders`** ➔ Hides orders / Duplicates orders / Falsely tells you the order is already "DELIVERED"
  * **`deleteOrder`** ➔ Randomly crashes / Fails to process your refund
* **`PaymentService.java`**
  * **`getPayments`** ➔ Randomly crashes when trying to load payment history
  * **`processPayment`** ➔ Charges you but doesn't save a receipt / Charges you twice / Payment succeeds but falsely tells the order it failed

