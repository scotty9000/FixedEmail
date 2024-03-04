package com.ulogic.fixed_email

data class EmailFields(
    val to: String = "To: ",
    val subject: String = "Subject: ",
    var address1: String = "",
    var subject1: String = "",
    var body1: String = ""
)
