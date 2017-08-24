package works.weave.socks.cart.controllers;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import works.weave.socks.cart.cart.CartDAO;
import works.weave.socks.cart.cart.CartResource;
import works.weave.socks.cart.entities.Item;
import works.weave.socks.cart.item.FoundItem;
import works.weave.socks.cart.item.ItemDAO;
import works.weave.socks.cart.item.ItemResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
@RequestMapping(value = "/carts/{customerId:.*}/items")
public class ItemsController {
    private final Logger LOG = getLogger(getClass());
	
    private static int sleep = 0;

    @Autowired
    private ItemDAO itemDAO;
    @Autowired
    private CartsController cartsController;
    @Autowired
    private CartDAO cartDAO;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{itemId:.*}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public Item get(@PathVariable String customerId, @PathVariable String itemId) {
    	try {
			Thread.sleep(this.getSleep());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return new FoundItem(() -> getItems(customerId), () -> new Item(itemId)).get();
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<Item> getItems(@PathVariable String customerId) {
    	try {
			Thread.sleep(this.getSleep());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return cartsController.get(customerId).contents();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Item addToCart(@PathVariable String customerId, @RequestBody Item item) {
    	try {
			Thread.sleep(this.getSleep());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	// If the item does not exist in the cart, create new one in the repository.
        FoundItem foundItem = new FoundItem(() -> cartsController.get(customerId).contents(), () -> item);
        if (!foundItem.hasItem()) {
            Supplier<Item> newItem = new ItemResource(itemDAO, () -> item).create();
            LOG.debug("Did not find item. Creating item for user: " + customerId + ", " + newItem.get());
            new CartResource(cartDAO, customerId).contents().get().add(newItem).run();
            return item;
        } else {
            Item newItem = new Item(foundItem.get(), foundItem.get().quantity() + 1);
            LOG.debug("Found item in cart. Incrementing for user: " + customerId + ", " + newItem);
            updateItem(customerId, newItem);
            return newItem;
        }
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @RequestMapping(value = "/{itemId:.*}", method = RequestMethod.DELETE)
    public void removeItem(@PathVariable String customerId, @PathVariable String itemId) {
    	try {
			Thread.sleep(this.getSleep());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	FoundItem foundItem = new FoundItem(() -> getItems(customerId), () -> new Item(itemId));
        Item item = foundItem.get();

        LOG.debug("Removing item from cart: " + item);
        new CartResource(cartDAO, customerId).contents().get().delete(() -> item).run();

        LOG.debug("Removing item from repository: " + item);
        new ItemResource(itemDAO, () -> item).destroy().run();
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PATCH)
    public void updateItem(@PathVariable String customerId, @RequestBody Item item) {
    	try {
			Thread.sleep(this.getSleep());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	// Merge old and new items
        ItemResource itemResource = new ItemResource(itemDAO, () -> get(customerId, item.itemId()));
        LOG.debug("Merging item in cart for user: " + customerId + ", " + item);
        itemResource.merge(item).run();
    }
    
    @RequestMapping(value = "/sleep/{sleep}", method = RequestMethod.GET)
    public Integer setSleep(@PathVariable int sleep) {
    	int oldSleep = this.sleep;
    	this.sleep = sleep;
    	return oldSleep;
    }
    
    private int getSleep() {
		URLConnection conn;
		try {
			conn = new URL("http://tobias-angerstein.de/sleep/carts").openConnection();
			conn.connect();

			try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String inputLine = in.readLine();
				if(inputLine.equals("Default")) {
					return sleep;
				}
				return Integer.parseInt(inputLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
