package com.carlsoncorp.hangmanservice.filter

import org.jboss.logging.MDC
import org.springframework.stereotype.Component
import java.io.IOException

import javax.servlet.ServletException

import javax.servlet.FilterChain

import javax.servlet.http.HttpServletResponse

import javax.servlet.http.HttpServletRequest

import org.springframework.web.filter.OncePerRequestFilter


/**
 * MDC Logging Filter.
 */
@Component
class MDCFilter : OncePerRequestFilter() {

    final val SESSION_ID_KEY = "sessionId"
    final val REQUEST_HEADER_SESSION_ID = "x-session-id"

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        MDC.put(SESSION_ID_KEY, request.getHeader(REQUEST_HEADER_SESSION_ID))
        filterChain.doFilter(request, response)
    }
}