package com.unisa.seedify.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDao extends BaseDao implements GenericDao<ProductBean> {
    public static final String TABLE_NAME = "prodotti";

    private static ProductDao instance = null;

    private ProductDao() {
    }

    public static ProductDao getInstance() {
        if (instance == null) {
            instance = new ProductDao();
        }
        return instance;
    }

    @Override
    public void doSave(ProductBean productBean) throws SQLException {
        String query = "INSERT INTO " + ProductDao.TABLE_NAME +
                       " (nome, immagine, prezzo, quantita, stagionalita, quantita_acqua, tipologia_pianta, descrizione, data_aggiunta)" +
                       " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, productBean.getName());
            preparedStatement.setBytes(2, productBean.getImage());
            preparedStatement.setFloat(3, productBean.getPrice());
            preparedStatement.setInt(4, productBean.getQuantity());
            preparedStatement.setString(5, productBean.getSeason().toString());
            preparedStatement.setString(6, productBean.getRequiredWater().toString());
            preparedStatement.setString(7, productBean.getPlantType());
            preparedStatement.setString(8, productBean.getDescription());
            preparedStatement.setDate(9, productBean.getAddedDate());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void doDelete(ProductBean productBean) throws SQLException {
        String query = "UPDATE " + ProductDao.TABLE_NAME +
                     " SET stato = 'ELIMINATO'" +
                     " WHERE codice_prodotto = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, productBean.getProductId());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void doUpdate(ProductBean productBean) throws SQLException {
        String query = "UPDATE " + ProductDao.TABLE_NAME +
                       " SET nome = ?, immagine = ?, prezzo = ?, quantita = ?, stagionalita = ?, quantita_acqua = ?, tipologia_pianta = ?, descrizione = ?" +
                       " WHERE codice_prodotto = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, productBean.getName());
            preparedStatement.setBytes(2, productBean.getImage());
            preparedStatement.setFloat(3, productBean.getPrice());
            preparedStatement.setInt(4, productBean.getQuantity());
            preparedStatement.setString(5, productBean.getSeason().toString());
            preparedStatement.setString(6, productBean.getRequiredWater().toString());
            preparedStatement.setString(7, productBean.getPlantType());
            preparedStatement.setString(8, productBean.getDescription());
            preparedStatement.setInt(9, productBean.getProductId());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public ProductBean doRetrive(EntityPrimaryKey primaryKey) throws SQLException {
        int productId = (int) primaryKey.getKey("codice_prodotto");

        String query = "SELECT * FROM " + ProductDao.TABLE_NAME +
                       " WHERE codice_prodotto = ?";

        ProductBean productBean = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, productId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    productBean = new ProductBean();
                    productBean.setProductId(resultSet.getInt("codice_prodotto"));
                    productBean.setName(resultSet.getString("nome"));
                    productBean.setImage(resultSet.getBytes("immagine"));
                    productBean.setPrice(resultSet.getFloat("prezzo"));
                    productBean.setQuantity(resultSet.getInt("quantita"));
                    productBean.setSeason(ProductBean.Seasons.fromString(resultSet.getString("stagionalita")));
                    productBean.setRequiredWater(ProductBean.RequiredWater.fromString(resultSet.getString("quantita_acqua")));
                    productBean.setPlantType(resultSet.getString("tipologia_pianta"));
                    productBean.setDescription(resultSet.getString("descrizione"));
                    productBean.setAddedDate(resultSet.getDate("data_aggiunta"));
                    productBean.setState(States.fromString(resultSet.getString("stato")));
                }
            }
        }
        return productBean;
    }

    public int getTotalProducts() {
        String query = "SELECT COUNT(*) AS products_count FROM " + ProductDao.TABLE_NAME;

        int productsAmount = 0;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query);) {

            if (resultSet.next()) {
                productsAmount = resultSet.getInt("products_count");
            }
        } catch (SQLException ignored) {}

        return productsAmount;
    }

    public List<ProductBean> getAllActiveProducts(String keywords) {
        String query = "SELECT * FROM " + ProductDao.TABLE_NAME +
                       "  WHERE stato = 'ATTIVO'";

        boolean keywordsPresent = keywords != null && !keywords.trim().isEmpty();
        if (keywordsPresent) {
            query += " AND (nome LIKE ? OR tipologia_pianta LIKE ?)";
        }

        List<ProductBean> products = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            if (keywordsPresent) {
                preparedStatement.setString(1, "%" + keywords + "%");
                preparedStatement.setString(2, "%" + keywords + "%");
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    ProductBean productBean = new ProductBean();
                    productBean.setProductId(resultSet.getInt("codice_prodotto"));
                    productBean.setName(resultSet.getString("nome"));
                    productBean.setImage(resultSet.getBytes("immagine"));
                    productBean.setPrice(resultSet.getFloat("prezzo"));
                    productBean.setQuantity(resultSet.getInt("quantita"));
                    productBean.setSeason(ProductBean.Seasons.fromString(resultSet.getString("stagionalita")));
                    productBean.setRequiredWater(ProductBean.RequiredWater.fromString(resultSet.getString("quantita_acqua")));
                    productBean.setPlantType(resultSet.getString("tipologia_pianta"));
                    productBean.setDescription(resultSet.getString("descrizione"));
                    productBean.setAddedDate(resultSet.getDate("data_aggiunta"));
                    productBean.setState(States.fromString(resultSet.getString("stato")));

                    products.add(productBean);
                }
            }

        } catch (SQLException ignored) {}

        return products;
    }

    public List<ProductBean> getAllActiveProducts() {
        return getAllActiveProducts(null);
    }

    public List<ProductBean> getAllActiveLatestProducts(int amount) {
        String query = "SELECT * FROM " + ProductDao.TABLE_NAME +
                       "  WHERE stato = 'ATTIVO'" +
                       "  ORDER BY data_aggiunta DESC" +
                       "  LIMIT ?";

        List<ProductBean> products = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, amount);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ProductBean productBean = new ProductBean();
                    productBean.setProductId(resultSet.getInt("codice_prodotto"));
                    productBean.setName(resultSet.getString("nome"));
                    productBean.setImage(resultSet.getBytes("immagine"));
                    productBean.setPrice(resultSet.getFloat("prezzo"));
                    productBean.setQuantity(resultSet.getInt("quantita"));
                    productBean.setSeason(ProductBean.Seasons.fromString(resultSet.getString("stagionalita")));
                    productBean.setRequiredWater(ProductBean.RequiredWater.fromString(resultSet.getString("quantita_acqua")));
                    productBean.setPlantType(resultSet.getString("tipologia_pianta"));
                    productBean.setDescription(resultSet.getString("descrizione"));
                    productBean.setAddedDate(resultSet.getDate("data_aggiunta"));
                    productBean.setState(States.fromString(resultSet.getString("stato")));

                    products.add(productBean);
                }
            }
        } catch (SQLException ignored) {}

        return products;
    }

    public List<ProductBean> getAllActiveMostPurchasedProducts(int amount) {
        String query = "SELECT * FROM " + ProductDao.TABLE_NAME +
                       " WHERE codice_prodotto IN ( " +
                       "    SELECT codice_prodotto FROM ( " +
                       "        SELECT COUNT(*) AS total_orders, codice_prodotto FROM " + GoodsDao.TABLE_NAME +
                       "        GROUP BY codice_prodotto " +
                       "        HAVING codice_prodotto IN ( " +
                       "            SELECT codice_prodotto FROM " + ProductDao.TABLE_NAME +
                       "            WHERE stato = 'ATTIVO' " +
                       "        ) ORDER BY total_orders DESC " +
                       "        LIMIT ? " +
                       "    ) AS most_purchased_products " +
                       " );";

        List<ProductBean> products = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, amount);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ProductBean productBean = new ProductBean();
                    productBean.setProductId(resultSet.getInt("codice_prodotto"));
                    productBean.setName(resultSet.getString("nome"));
                    productBean.setImage(resultSet.getBytes("immagine"));
                    productBean.setPrice(resultSet.getFloat("prezzo"));
                    productBean.setQuantity(resultSet.getInt("quantita"));
                    productBean.setSeason(ProductBean.Seasons.fromString(resultSet.getString("stagionalita")));
                    productBean.setRequiredWater(ProductBean.RequiredWater.fromString(resultSet.getString("quantita_acqua")));
                    productBean.setPlantType(resultSet.getString("tipologia_pianta"));
                    productBean.setDescription(resultSet.getString("descrizione"));
                    productBean.setAddedDate(resultSet.getDate("data_aggiunta"));
                    productBean.setState(States.fromString(resultSet.getString("stato")));

                    products.add(productBean);
                }
            }
        } catch (SQLException ignored) {}

        return products;
    }

    public List<ProductBean> getAllActiveCategoryProducts(int amount, String category) {
        String query = "SELECT * FROM " + ProductDao.TABLE_NAME +
                       " WHERE tipologia_pianta = ? AND stato = 'ATTIVO'" +
                       " ORDER BY data_aggiunta DESC" +
                       " LIMIT ?";

        List<ProductBean> products = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, category);
            statement.setInt(2, amount);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ProductBean productBean = new ProductBean();
                    productBean.setProductId(resultSet.getInt("codice_prodotto"));
                    productBean.setName(resultSet.getString("nome"));
                    productBean.setImage(resultSet.getBytes("immagine"));
                    productBean.setPrice(resultSet.getFloat("prezzo"));
                    productBean.setQuantity(resultSet.getInt("quantita"));
                    productBean.setSeason(ProductBean.Seasons.fromString(resultSet.getString("stagionalita")));
                    productBean.setRequiredWater(ProductBean.RequiredWater.fromString(resultSet.getString("quantita_acqua")));
                    productBean.setPlantType(resultSet.getString("tipologia_pianta"));
                    productBean.setDescription(resultSet.getString("descrizione"));
                    productBean.setAddedDate(resultSet.getDate("data_aggiunta"));
                    productBean.setState(States.fromString(resultSet.getString("stato")));

                    products.add(productBean);
                }
            }
        } catch (SQLException ignored) {}

        return products;
    }
}
