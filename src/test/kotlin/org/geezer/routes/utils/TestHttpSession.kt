package org.geezer.routes.utils

import java.util.*
import jakarta.servlet.ServletContext
import jakarta.servlet.http.HttpSession
import jakarta.servlet.http.HttpSessionContext
import kotlin.collections.HashMap

class TestHttpSession : HttpSession {
    var attributes: MutableMap<String, Any> = HashMap()

    var values: MutableMap<String, Any> = HashMap()

    private val creationTime = System.currentTimeMillis()

    override fun getCreationTime(): Long {
        return creationTime
    }

    override fun getId(): String? {
        return "ID"
    }

    override fun getLastAccessedTime(): Long {
        throw RuntimeException("Not implemented.")
    }

    override fun getServletContext(): ServletContext? {
        throw RuntimeException("Not implemented.")
    }

    override fun setMaxInactiveInterval(interval: Int) {}

    override fun getMaxInactiveInterval(): Int {
        throw RuntimeException("Not implemented.")
    }

    override fun getSessionContext(): HttpSessionContext? {
        throw RuntimeException("Not implemented.")
    }

    override fun getAttribute(name: String): Any? {
        return attributes[name]
    }

    override fun getValue(name: String): Any? {
        return values[name]
    }

    override fun getAttributeNames(): Enumeration<String>? {
        return Collections.enumeration(attributes.keys)
    }

    override fun getValueNames(): Array<String>? {
        val names: MutableList<String> = ArrayList()
        for (name in values.keys) names.add(name)
        return names.toTypedArray()
    }

    override fun setAttribute(name: String, value: Any) {
        attributes[name] = value
    }

    override fun putValue(name: String, value: Any) {
        values[name] = value
    }

    override fun removeAttribute(name: String) {
        attributes.remove(name)
    }

    override fun removeValue(name: String) {
        values.remove(name)
    }

    override fun invalidate() {}

    override fun isNew(): Boolean {
        return true
    }
}