package com.springshop.springshop.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mantiene las líneas del carrito de una sesión; no comparte su colección con otros usuarios.
 */
public class Carrito {

	private final ArrayList<ItemCarrito> items = new ArrayList<>();

	/**
	 * Agrega una unidad o incrementa la línea existente sin superar el stock disponible.
	 */
	public void agregarProducto(Producto producto) {
		Objects.requireNonNull(producto, "El producto es obligatorio");
		ItemCarrito item = buscarItem(producto.getId());
		int cantidadActual = item == null ? 0 : item.getCantidad();

		if (cantidadActual >= producto.getStock()) {
			throw new IllegalArgumentException("No hay stock suficiente para agregar otra unidad");
		}

		if (item == null) {
			items.add(new ItemCarrito(producto, 1));
		} else {
			item.incrementarCantidad();
		}
	}

	/**
	 * Devuelve cuántas unidades de un producto están actualmente en el carrito.
	 */
	public int obtenerCantidad(Long productoId) {
		ItemCarrito item = buscarItem(productoId);
		return item == null ? 0 : item.getCantidad();
	}

	/**
	 * Devuelve una copia no modificable para evitar cambios desde la capa web.
	 */
	public List<ItemCarrito> getItems() {
		return List.copyOf(items);
	}

	/**
	 * Suma los subtotales de todas las líneas del carrito.
	 */
	public BigDecimal calcularTotal() {
		return items.stream()
				.map(ItemCarrito::getSubtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getTotal() {
		return calcularTotal();
	}

	private ItemCarrito buscarItem(Long productoId) {
		return items.stream()
				.filter(item -> Objects.equals(item.getProducto().getId(), productoId))
				.findFirst()
				.orElse(null);
	}
}
