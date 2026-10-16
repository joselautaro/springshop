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
			item.incrementarCantidad(producto.getStock());
		}
	}

	/**
	 * Aumenta una unidad usando el stock del producto guardado en la línea.
	 */
	public void aumentarCantidad(Long idProducto) {
		ItemCarrito item = buscarItemRequerido(idProducto);
		item.incrementarCantidad(item.getProducto().getStock());
	}

	/**
	 * Aumenta una unidad respetando el stock más reciente consultado en el catálogo.
	 */
	public void aumentarCantidad(Long idProducto, int stockVigente) {
		ItemCarrito item = buscarItemRequerido(idProducto);
		item.incrementarCantidad(stockVigente);
	}

	/**
	 * Disminuye una unidad; la línea conserva como mínimo una unidad.
	 */
	public void disminuirCantidad(Long idProducto) {
		buscarItemRequerido(idProducto).disminuirCantidad();
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

	private ItemCarrito buscarItemRequerido(Long productoId) {
		if (productoId == null || productoId <= 0) {
			throw new IllegalArgumentException("El identificador del producto no es válido");
		}
		ItemCarrito item = buscarItem(productoId);
		if (item == null) {
			throw new IllegalArgumentException("El producto no está presente en el carrito");
		}
		return item;
	}
}
