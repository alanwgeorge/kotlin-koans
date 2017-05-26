package iii_conventions

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.*

class _28_For_Loop {
    @Test fun testIterateOverDateRange() {
        val actualDateRange = ArrayList<MyDate>()
        iterateOverDateRange(MyDate(2014, 5, 1), MyDate(2014, 5, 5), {
            date: MyDate -> actualDateRange.add(date)
        })
        val expectedDateRange = arrayListOf(
                MyDate(2014, 5, 1), MyDate(2014, 5, 2), MyDate(2014, 5, 3), MyDate(2014, 5, 4), MyDate(2014, 5, 5))
        assertEquals("Incorrect iteration over five nice spring dates",
                expectedDateRange, actualDateRange)
    }

    @Test fun testIterateOverEmptyRange() {
        var invoked = false
        iterateOverDateRange(MyDate(2014, 1, 1), MyDate(2013, 1, 1), { invoked = true })
        assertFalse("Handler was invoked on an empty range", invoked)
    }

    @Test fun testFoo() {
        fun foo(init: Foo.() -> Unit): Foo {
            val foo = Foo("helloworld")
            foo.init();
            return foo
        }

        val f = foo { print("$name\n") }
    }

    data class Foo(val name: String)

    @Test fun testPayload() {
        val d = mapOf<String, String>("a" to "b", "c" to "d")

        val p = Payload.create {
            nonce ="123456"
            action = "Action"
            data = d
        }

        val gson: Gson = GsonBuilder().create()

        print("${gson.toJson(p, Payload::class.java)}\n")
    }

    class Payload private constructor(@Expose val nonce: String, @Expose val action: String, @Expose val data: Any) {
        private constructor(builder: Builder): this(builder.nonce, builder.action, builder.data)

        companion object {
            fun create(init: Builder.() -> Unit) = Builder(init).build()
        }


        override fun toString(): String {
            return "Payload(nonce='$nonce', action='$action', data=$data)"
        }

        class Builder private constructor() {

            constructor(init: Builder.() -> Unit): this() {
                init()
            }
            lateinit var nonce: String
            lateinit var action: String

            lateinit var data: Any
            fun nonce(init: Builder.() -> String) { nonce = init() }
            fun action(init: Builder.() -> String) { action = init() }

            fun data(init: Builder.() -> Any) { data = init() }
            fun build() = Payload(this)

        }
    }
}