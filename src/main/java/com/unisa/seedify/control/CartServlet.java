package com.unisa.seedify.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.unisa.seedify.model.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "cartServlet", urlPatterns = {"/cart-servlet"})
public class CartServlet extends HttpServlet implements JsonServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean skipJsonBody = false;
        JsonObject jsonBody = null;
        try {
            jsonBody = JsonServlet.parsePostRequestBody(request);
        } catch (IllegalStateException e) {
            skipJsonBody = true;
        }

        HttpSession httpSession = request.getSession(true);
        CartBean cartBean = (CartBean) httpSession.getAttribute("cart");

        String action = null;
        try {
            if (skipJsonBody) {
                action = request.getParameter("action");
            } else {
                action = jsonBody.get("action").getAsString();
            }
        } catch (NullPointerException e) {
            if (!skipJsonBody) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing param 'action' in request body");
                return;
            }
        }

        Integer productId = null;
        try {
            productId = jsonBody.get("product_id").getAsInt();
        } catch (NullPointerException | JsonSyntaxException e) {
            if (!skipJsonBody) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing param 'product_id' in request body");
                return;
            }
        }

        ProductBean productBean = null;
        if (!skipJsonBody) {
            try {
                EntityPrimaryKey productPrimaryKey = new EntityPrimaryKey();
                productPrimaryKey.addKey("codice_prodotto", productId);
                productBean = productDao.doRetrive(productPrimaryKey);
            } catch (SQLException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while processing the request");
                return;
            }
        }

        boolean success = false;
        switch (action) {
            case "add_to_cart": {
                try {
                    if (productBean == null) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Product not found");
                        return;
                    }

                    int quantity = jsonBody.get("quantity").getAsInt();
                    if (quantity > 0) {
                        success = cartDao.addToCart(cartBean, productBean, quantity);
                    } else if (quantity < 0) {
                        success = cartDao.removeFromCart(cartBean, productBean, -quantity);
                    } else {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid quantity");
                    }
                } catch (NullPointerException | JsonSyntaxException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing param 'product_id' in request body");
                }
                break;
            }
            case "empty_cart": {
                try {
                    cartDao.emptyCart(cartBean);
                    success = true;
                    response.sendRedirect("cart");
                    return;
                } catch (SQLException e) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while processing the request");
                }
                break;
            }
            default: {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        }

        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "An error occurred while processing the request");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        CartBean cartBean = (CartBean) session.getAttribute("cart");

        String action = request.getParameter("action");
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter 'action'");
            return;
        }

        switch (action) {
            case "remove_from_cart": {
                try {
                    int deleteProductId = Integer.parseInt(request.getParameter("product_id"));

                    List<CartItemBean> cartItems = cartBean.getCartItems();
                    CartItemBean cartItemBeanToRemove = null;
                    for (CartItemBean cartItemBean : cartItems) {
                        if (cartItemBean.getProduct().getProductId() == deleteProductId) {
                            cartItemBeanToRemove = cartItemBean;
                            break;
                        }
                    }

                    if (cartItemBeanToRemove != null) {
                        cartDao.doDeleteOne(cartBean, cartItemBeanToRemove);
                        cartBean.getCartItems().remove(cartItemBeanToRemove);
                        session.setAttribute("cart", cartBean);
                    } else {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot remove product ' " + deleteProductId + " ' product from cart");
                    }
                } catch (SQLException ignored) {}
                break;
            }
            default: {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        }
    }
}
