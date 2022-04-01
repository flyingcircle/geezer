package org.geezer.layouts

object Layouts {
    /**
     * To disable any layout for the current request do:
     * httpServletRequest.setAttribute(Layouts.NO_LAYOUT, true);
     */
    const val NO_LAYOUT = "noLayout"

    /**
     * To override the default layout for the current request do:
     * httpServletRequest.setAttribute(Layouts.LAYOUT, "customLayout");
     * The name of the layout should be the file name minus the file extension.
     */
    const val LAYOUT = "layout"

    /**
     * The view can be rendered in a layout in one of two ways:
     * <html>
     *   <head>${view.yieldHead(pageContext)}</head>
     *   <body><% ((View)request.getAttribute(Layouts.VIEW)).yieldBody(pageContext); %></body>
     * </html>
     */
    const val VIEW = "view"
}
