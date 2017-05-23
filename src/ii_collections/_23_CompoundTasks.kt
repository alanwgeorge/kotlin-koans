package ii_collections

fun Shop.getCustomersWhoOrderedProduct(product: Product): Set<Customer> =
    customers.filter { it.orders.flatMap { it.products } .any { it == product } } .toSet()

fun Customer.getMostExpensiveDeliveredProduct(): Product? =
        orders.filter { it.isDelivered } .flatMap { it.products } .maxBy { it.price }

fun Shop.getNumberOfTimesProductWasOrdered(product: Product): Int =
        customers.flatMap { it.orders } .flatMap { it.products } .filter { it == product } .count()
