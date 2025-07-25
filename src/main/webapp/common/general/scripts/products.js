import { getBaseOriginName, sendAjaxRequest } from "./script.js";
import { toast } from "./toast.js";

export { getProductCard, sendAddToFavoriteRequest, sendRemoveFromFavoriteRequest, sendAddToCartRequest, goToProductPage };

/**
 * Sends a request to add a product to the favorites.
 * @param {number} productId
 * @param {HTMLButtonElement} favoriteButton
 */
function sendAddToFavoriteRequest(productId, favoriteButton) {
    const body = {
        action: "add_to_favorites",
        product_id: productId
    };

    sendAjaxRequest(
        "POST",
        `${getBaseOriginName()}/favorites-servlet`,
        JSON.stringify(body),
        {
            200: function () {
                favoriteButton.getElementsByTagName("span")[0].innerHTML = "favorite";
                toast("Aggiunto ai preferiti", "SUCCESS");
            }
        }
    );
}

/**
 * Sends a request to remove a product from the favorites.
 * @param {number} productId
 * @param {HTMLButtonElement} favoriteButton
 */
function sendRemoveFromFavoriteRequest(productId, favoriteButton) {
    sendAjaxRequest(
        "DELETE",
        `${getBaseOriginName()}/favorites-servlet?action=remove_from_favorites&product_id=${productId}`,
        null,
        {
            200: function () {
                favoriteButton.getElementsByTagName("span")[0].innerHTML = "favorite_border";
                toast("Rimosso dai preferiti", "SUCCESS");
            }
        }
    );
}

/**
 * Sends a request to add a product to the cart.
 * @param {number} productId
 * @param {number} quantity
 * @param {function} callback
 */
function sendAddToCartRequest(productId, quantity, callback) {
    const body = {
        action: "add_to_cart",
        product_id: productId,
        quantity: quantity
    };

    sendAjaxRequest(
        "POST",
        `${getBaseOriginName()}/cart-servlet`,
        JSON.stringify(body),
        {
            200: function () {
                const counter = document.querySelector("#cart-items-counter span");
                counter.innerHTML = `${parseInt(counter.innerHTML) + quantity}`;
                if (quantity > 0) {
                    toast("Aggiunto al carrello", "SUCCESS");
                } else {
                    toast("Rimosso dal carrello", "SUCCESS");
                }
                if (callback !== null) {
                    callback();
                }
            }
        }
    )
}

/**
 * Redirects to the product page.
 * @param {number} productId
 */
function goToProductPage(productId) {
    window.location.href = `${getBaseOriginName()}/resources-servlet?resource_type=product_page&product_id=${productId}`;
}

/**
 * Returns a product card.
 * @param {string} name
 * @param {number} price
 * @param {string} image
 * @param {number} productId
 * @param {boolean} isFavorite
 * @returns {HTMLDivElement}
 */
function getProductCard(name, price, image, productId, isFavorite) {
    const productContainer = document.createElement("div");
    productContainer.classList.add("product-container", "ui-block", "rubik-300");

    // Creating favorite button
    const favoriteButton = document.createElement("button");
    favoriteButton.classList.add("favorite-button", "material-button");
    favoriteButton.addEventListener(
        'click',
        function () {
            const iconContent = favoriteButton.getElementsByTagName("span")[0].innerHTML;
            if (iconContent === "favorite") {
                sendRemoveFromFavoriteRequest(productId, favoriteButton);
            } else if (iconContent === "favorite_border") {
                sendAddToFavoriteRequest(productId, favoriteButton);
            }
        }
    )

    const favoriteIcon = document.createElement("span");
    favoriteIcon.classList.add("material-icons-round", "md-18");
    if (isFavorite) {
        favoriteIcon.innerHTML = "favorite";
    } else {
        favoriteIcon.innerHTML = "favorite_border";
    }

    // Creating product image section
    const productImageSection = document.createElement("div");
    productImageSection.classList.add("product-image-section");

    const productImageContainer = document.createElement("div")
    productImageContainer.classList.add("product-image-container");
    productImageContainer.addEventListener('click', function() {
        goToProductPage(productId);
    });

    const productImage = document.createElement("img");
    productImage.src = image;

    // Creating product info section
    const productInfoSection = document.createElement("div");
    productInfoSection.classList.add("product-info-section");

    const productInfoContainer = document.createElement("div");
    productInfoContainer.classList.add("product-info-container");

    const productInfo = document.createElement("div");
    productInfo.classList.add("product-info");
    productInfo.addEventListener('click', function() {
        goToProductPage(productId);
    });

    const nameParagraph = document.createElement("p");
    nameParagraph.classList.add("product-name", "rubik-500");
    nameParagraph.innerHTML = name;

    const priceParagraph = document.createElement("p");
    priceParagraph.classList.add("product-price");
    priceParagraph.innerHTML = `${price} €`;

    const productAction = document.createElement("div");
    productAction.classList.add("product-actions");

    const cartButton = document.createElement("button");
    cartButton.classList.add("cart-button", "material-button");
    cartButton.addEventListener(
        'click',
        function () {
            sendAddToCartRequest(productId, 1);
        }
    )

    const cartIcon = document.createElement("span");
    cartIcon.classList.add("material-icons-round", "md-18");
    cartIcon.innerHTML = "add_shopping_cart";

    const addToCartText = document.createElement("span");
    addToCartText.classList.add("small-text");
    addToCartText.innerHTML = "Aggiungi al carrello";

    productContainer.appendChild(favoriteButton);
    favoriteButton.appendChild(favoriteIcon);

    productContainer.appendChild(productImageSection);
    productContainer.appendChild(productInfoSection);

    productImageSection.appendChild(productImageContainer);
    productImageContainer.appendChild(productImage);

    productInfoSection.appendChild(productInfoContainer);
    productInfoContainer.appendChild(productInfo);
    productInfoContainer.appendChild(productAction);

    productInfo.appendChild(nameParagraph);
    productInfo.appendChild(priceParagraph);

    productAction.appendChild(cartButton);
    cartButton.appendChild(cartIcon);
    cartButton.appendChild(addToCartText);

    return productContainer;
}