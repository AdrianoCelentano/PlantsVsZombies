package com.clean.plantsvszombies

data class Plant(
    val fixed: Boolean = false,
    val href: String = "https://res.cloudinary.com/teepublic/image/private/s--W8qssCEh--/t_Preview/b_rgb:0f7b47,c_limit,f_jpg,h_630,q_90,w_630/v1536242932/production/designs/3118070_0.jpg",
    val id: Int = (Math.random() * 1000000).toInt(),
    val lat: Double = 0.0,
    val long: Double = 0.0,
    val solution: String? = null
)