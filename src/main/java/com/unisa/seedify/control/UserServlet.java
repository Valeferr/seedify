package com.unisa.seedify.control;

import com.google.gson.*;
import com.unisa.seedify.utils.JsonUtils;
import com.unisa.seedify.model.*;
import com.unisa.seedify.utils.SecurityUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.*;

@WebServlet(name = "userServlet", value = "/user-servlet")
public class UserServlet extends HttpServlet implements JsonServlet {
    private static class TableDataResponse {
        private final boolean canEdit;
        private final String editCall;
        private final boolean canDelete;
        private final String deleteCall;
        private final List<Object> data;

        public TableDataResponse(boolean canEdit, String editCall, boolean canDelete, String deleteCall, List<Object> data) {
            this.canEdit = canEdit;
            this.editCall = editCall;
            this.canDelete = canDelete;
            this.deleteCall = deleteCall;
            this.data = data;
        }
    }

    private static String ENCRYPTION_KEY = "";

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            InitialContext initialContext = new InitialContext();
            Context environmentContext = (Context) initialContext.lookup("java:/comp/env");
            ENCRYPTION_KEY = (String) environmentContext.lookup("dataEncryptionKey");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        ArrayList<String> fields = new ArrayList<>(Arrays.asList(request.getParameter("fields").split(",")));
        fields.add("entity_primary_key");

        UserBean userBean = (UserBean) request.getSession(true).getAttribute("user");

        String dataName = "";
        boolean canEdit = false;
        String editCall = null;
        boolean canDelete = false;
        String deleteCall = null;

        ArrayList<Object> data = null;
        if (userBean.getRole().equals(UserBean.Roles.ADMIN)) {
            switch (action) {
                case "get_customers" : {
                    data = new ArrayList<>(userDao.getAllCustomers());
                    dataName = "customers";
                    break;
                }
                case "get_orders": {
                    String startDate = request.getParameter("start_date");
                    String endDate = request.getParameter("end_date");

                    data = new ArrayList<>(orderDao.getAllOrders());

                    if (startDate != null && !startDate.isEmpty()) {
                        Date start = Date.valueOf(startDate);
                        Date end = (endDate != null && !endDate.isEmpty()) ? Date.valueOf(endDate) : null;
                        data.removeIf(order -> {
                            Date orderDate = ((OrderBean) order).getOrderDate();
                            return !(orderDate.after(start) && (end == null || orderDate.before(end)));
                        });
                    }

                    dataName = "all_users_orders";
                    break;
                }
                case "get_products": {
                    canEdit = true;
                    editCall = "product-servlet?action=edit_product";
                    canDelete = true;
                    deleteCall = "product-servlet?action=delete_product";

                    data = new ArrayList<>(productDao.getAllActiveProducts());
                    dataName = "all_saved_products";
                    break;
                }
                default: {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                }
            }
        } else if (userBean.getRole().equals(UserBean.Roles.CUSTOMER)) {
            switch (action) {
                case "get_orders": {
                    String startDate = request.getParameter("start_date");
                    String endDate = request.getParameter("end_date");

                    data = new ArrayList<>(orderDao.getAllOrders(userBean));

                    if (startDate != null && !startDate.isEmpty()) {
                        Date start = Date.valueOf(startDate);
                        Date end = (endDate != null && !endDate.isEmpty()) ? Date.valueOf(endDate) : null;
                        data.removeIf(order -> {
                            Date orderDate = ((OrderBean) order).getOrderDate();
                            return !(orderDate.after(start) && (end == null || orderDate.before(end)));
                        });
                    }

                    dataName = "user_orders";
                    break;
                }
                case "get_favorites": {
                    deleteCall = "favorites-servlet?action=remove_from_favorites";
                    canDelete = true;

                    data = new ArrayList<>(favoritesDao.getUserFavorites(userBean).getProducts());
                    dataName = "user_favorites_products";
                    break;
                }
                case "get_credit_cards": {
                    data = new ArrayList<>(memorizationsDao.getAllCreditCards(userBean));
                    dataName = "user_credit_cards";
                    break;
                }
                case "get_addresses": {
                    data = new ArrayList<>(locationsDao.getAllAddresses(userBean));
                    dataName = "user_addresses";
                    break;
                }
                default: {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                }
            }
        }

        TableDataResponse tableDataResponse = new TableDataResponse(canEdit, editCall, canDelete, deleteCall, data);
        JsonObject jsonResponseObject = this.gson.toJsonTree(tableDataResponse).getAsJsonObject();

        JsonArray rawData = jsonResponseObject.get("data").getAsJsonArray();
        JsonArray filteredData = JsonUtils.filterJsonArray(rawData, fields);
        jsonResponseObject.remove("data");
        jsonResponseObject.add("data", filteredData);
        jsonResponseObject.add("data_name", new JsonPrimitive(dataName));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.print(jsonResponseObject);
        out.flush();
    }
}
