package org.example.isometricgame

import io.ygdrasil.webgpu.Color
import io.ygdrasil.webgpu.ColorTargetState
import io.ygdrasil.webgpu.CompositeAlphaMode
import io.ygdrasil.webgpu.FragmentState
import io.ygdrasil.webgpu.GPUDevice
import io.ygdrasil.webgpu.GPULoadOp
import io.ygdrasil.webgpu.GPURenderPipeline
import io.ygdrasil.webgpu.GPUStoreOp
import io.ygdrasil.webgpu.GPUTexture
import io.ygdrasil.webgpu.RenderPassColorAttachment
import io.ygdrasil.webgpu.RenderPassDescriptor
import io.ygdrasil.webgpu.RenderPipelineDescriptor
import io.ygdrasil.webgpu.ShaderModuleDescriptor
import io.ygdrasil.webgpu.SurfaceConfiguration
import io.ygdrasil.webgpu.VertexState
import io.ygdrasil.webgpu.WGPUContext
import io.ygdrasil.webgpu.autoClosableContext
import io.ygdrasil.webgpu.beginRenderPass

//external fun setInterval(org.example.isometricgame.render: () -> Unit, updateInterval: Int)
//val UPDATE_INTERVAL = (1000.0 / 60.0).toInt()

class Renderer(val wgpuContext: WGPUContext) {
    val texture : GPUTexture
        get() = wgpuContext.renderingContext.getCurrentTexture()

    val device: GPUDevice
        get() = wgpuContext.device

    init {
        wgpuContext.configureRenderingContext()
    }

    suspend fun render()  = autoClosableContext {
        val renderPipeline = getRenderPipeline()

        val descriptor = RenderPassDescriptor(
            colorAttachments = listOf(
                RenderPassColorAttachment(
                    view = texture.createView().bind(),
                    loadOp = GPULoadOp.Clear,
                    storeOp = GPUStoreOp.Store,
                    clearValue = Color(0.0,0.0,1.0, 1.0)
                )
            )
        )
        val encoder = device.createCommandEncoder()
        encoder.beginRenderPass(
            descriptor = descriptor,
        ) {
            setPipeline(renderPipeline)
            draw(3u)
            end()
        }
        val commandBuffer = encoder.finish().bind()

        device.queue.submit(listOf(commandBuffer))
    }

    private suspend fun getRenderPipeline() : GPURenderPipeline = autoClosableContext {
        val descriptor = RenderPipelineDescriptor(
            vertex = VertexState(
                entryPoint = "main",
                module = device.createShaderModule(
                    ShaderModuleDescriptor(
                        code = triangleVertexShader
                    )
                ).bind()
            ),
            fragment = FragmentState(
                entryPoint = "main",
                module = device.createShaderModule(
                    ShaderModuleDescriptor(
                        code = redFragmentShader
                    )
                ).bind(),
                targets = listOf(
                    ColorTargetState(
                        format = wgpuContext.renderingContext.textureFormat,
                    )
                )
            ),
        )
        return@autoClosableContext device.createRenderPipeline(
            descriptor = descriptor,
        )
    }

}

private fun WGPUContext.configureRenderingContext() {
    val format = renderingContext.textureFormat
    val alphaMode = CompositeAlphaMode.Inherit.takeIf { surface.supportedAlphaMode.contains(it) }
        ?: CompositeAlphaMode.Opaque

    surface.configure(
        SurfaceConfiguration(
            device = device,
            format = format,
            alphaMode = alphaMode
        )
    )
}
