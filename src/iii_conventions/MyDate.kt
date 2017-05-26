package iii_conventions

data class MyDate(val year: Int, val month: Int, val dayOfMonth: Int): Comparable<MyDate> {
    override fun compareTo(other: MyDate): Int = when {
        year != other.year -> year - other.year
        month != other.month -> month -other.month
        else -> dayOfMonth - other.dayOfMonth
    }
}

operator fun MyDate.rangeTo(other: MyDate): DateRange = DateRange(this, other)

operator fun MyDate.plus(timeInterval: TimeInterval): MyDate = addTimeIntervals(timeInterval, 1)

operator fun MyDate.plus(timeIntervals: RepestedTimeIntervals): MyDate = addTimeIntervals(timeIntervals.timeInterval, timeIntervals.times)

enum class TimeInterval {
    DAY,
    WEEK,
    YEAR
}

class RepestedTimeIntervals(val timeInterval: TimeInterval, val times: Int)

operator fun TimeInterval.times(times: Int): RepestedTimeIntervals = RepestedTimeIntervals(this, times)

class DateRange(override val start: MyDate, override val endInclusive: MyDate): ClosedRange<MyDate>, Iterable<MyDate> {
    override fun iterator(): Iterator<MyDate> = object : Iterator<MyDate> {
        var current: MyDate = start

        override fun hasNext(): Boolean = current <= endInclusive

        override fun next(): MyDate {
            val result = current
            current = current.nextDay()
            return result
        }
    }
}

