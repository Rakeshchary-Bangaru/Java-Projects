# ECommerceOrderEngine V11 — Sample Product Images

This pack contains **18 synthetic demonstration product pictures** in six example categories. They were cropped from AI-generated catalog artwork for testing, and are not photos of real merchandise.

## Contents

- `electronics/`: laptop, headphones, keyboard
- `home-appliances/`: refrigerator, washing machine, microwave
- `mobiles/`: smartphone, tablet, smartwatch
- `fashion/`: t-shirt, jeans, sneakers
- `books/`: Java, Spring Boot, DSA books (fictional illustrated covers)
- `sports/`: football, cricket bat, yoga mat

All files are valid PNG images. `image-manifest.csv` lists paths and suggested product names.

## Important — importing into V11

1. These are **example** categories. Your actual categories are free-form `Product.category` values; adjust names/mappings to match your database.
2. **Do not copy these files directly** into `local-uploads/product-images/` and expect them to appear. Your application stores files using generated storage keys and saves matching metadata records in `product_images`.
3. Preferred test: log in as ROOT, open Admin > Products > Manage Images, and upload a PNG to the corresponding existing product using the API/UI. The upload automatically validates/re-encodes the image, stores it with a safe key, and creates a `product_images` record.
4. Uploaded images become visible on `/shop/products` and on homepage featured products **only if those products are among the first three returned by `getProducts()`**.
5. Do not use Flyway migration to bulk-insert file paths alone. A proper data migration requires copying files to app storage and persisting image metadata transactionally, handling ownership and rollback.
6. Do not use this test pack as production marketing imagery; review suitable licensed product photographs before deployment.
