package br.com.gunthercloud.telegramapi.service;

import br.com.gunthercloud.telegramapi.domain.ProductModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class ProductService {

    @Autowired
    private ProductModel product;

    public ProductModel createProduct(ProductModel product) {
        ProductModel p = new ProductModel();
        BeanUtils.copyProperties(product, p);
        System.out.println(p.getName());
        return p;
    }
}
