
package com.example.reto8

data class Contact(val name: String, val classification: Int){
    override fun toString(): String {
        return name
    }
}