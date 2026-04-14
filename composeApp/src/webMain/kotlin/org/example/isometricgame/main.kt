package org.example.isometricgame

import io.ygdrasil.webgpu.HTMLCanvasElement
import io.ygdrasil.webgpu.canvasContextRenderer
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

fun main() {
    MainScope().launch {

        val canvas = (document.getElementById("canvas") as? HTMLCanvasElement)
        val wgpuContext = canvasContextRenderer(canvas).wgpuContext

        val renderer = Renderer(wgpuContext)
        renderer.render()
    }
}

