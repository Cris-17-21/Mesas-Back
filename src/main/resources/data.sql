-- Tipos de pago iniciales para el módulo de Compras
-- INSERT IGNORE no falla si el registro ya existe
INSERT IGNORE INTO tipospago (id_tipopago, tipo_pago) VALUES
(1, 'Efectivo'),
(2, 'Transferencia Bancaria'),
(3, 'Cheque'),
(4, 'Tarjeta de Crédito'),
(5, 'Tarjeta de Débito'),
(6, 'Crédito 30 días'),
(7, 'Crédito 60 días'),
(8, 'Crédito 90 días');
