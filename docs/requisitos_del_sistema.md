# Especificación de Requisitos Funcionales y No Funcionales (SGT)

Este documento detalla la lista oficial de **Requisitos Funcionales (RF)**, **Reglas de Negocio (RN)**, **Requisitos de Interfaz (RI)**, **Requisitos de Datos (RD)** y **Requisitos No Funcionales (RNF)** para el **Sistema de Gestión de Turnos Universitarios (SGT)**, basados en el documento base *ERS-SGT-001*.

## 1. Requisitos Funcionales (RF)

| ID | Nombre | Descripción | Criterio de Verificación / Prueba | 
| ----- | ----- | ----- | ----- | 
| **RF-01** | Consultar ventanillas | Mostrar las ventanillas `V01`, `V02` y `V03` indicando si están libres o qué turno se encuentra en atención. | Con un turno activo en `V02`, la consulta muestra `V02` ocupada y `V01`/`V03` libres. | 
| **RF-02** | Registrar turno | Crear un turno con ID y secuencia únicos, fecha/hora de registro, estado *En espera* y ventanilla sin asignar; incorporarlo al final de la cola. | Registrar tres turnos y comprobar el orden y la unicidad de los identificadores. | 
| **RF-03** | Consultar fila | Presentar los turnos *En espera* en orden ascendente de secuencia, incluyendo ID y fecha/hora de registro. | Crear varios turnos, asignar y cancelar algunos, y comprobar que la cola solo incluye pendientes en orden FIFO. | 
| **RF-04** | Asignar siguiente turno | Al solicitar atención desde una ventanilla libre, seleccionar el turno *En espera* de menor secuencia, cambiar su estado a *En atención* y asociarlo exclusivamente a esa ventanilla. | Registrar T1, T2, T3; asignar desde V03 y después V01: deben asignarse T1 y T2 respectivamente. | 
| **RF-05** | Rechazar asignaciones imposibles | Impedir la asignación cuando la ventanilla seleccionada esté ocupada o no existan turnos *En espera*, mostrando explicación y conservando el estado previo. | Intentar asignar sobre ventanilla ocupada y con fila vacía; verificar que nada cambie. | 
| **RF-06** | Finalizar atención | Permitir finalizar el turno *En atención* en una ventanilla seleccionada, cambiarlo a *Atendido* y dejar libre la ventanilla. | Finalizar en V01 y comprobar estado del turno y disponibilidad de V01. | 
| **RF-07** | Rechazar finalización inválida | Impedir finalizar una atención desde una ventanilla libre, sin modificar otros turnos o ventanillas. | Solicitar finalizar en V02 libre; verificar mensaje y ausencia de cambios. | 
| **RF-08** | Cancelar turno pendiente | Permitir cancelar un turno únicamente si su estado es *En espera*, cambiándolo a *Cancelado* y retirándolo de la fila sin eliminar su historial. | Cancelar T2 en espera; comprobar que se excluye de la cola y permanece en el historial. | 
| **RF-09** | Rechazar cancelación inválida | Rechazar la cancelación de un ID inexistente o de un turno que no esté *En espera*, preservando el estado de la aplicación. | Intentar cancelar ID inexistente y turnos *En atención*, *Atendido* y *Cancelado*. | 
| **RF-10** | Consultar historial simple | Permitir consultar todos los turnos registrados (*En espera*, *En atención*, *Atendido*, *Cancelado*), mostrando ID, fecha/hora, estado y ventanilla asignada. | Crear un turno de cada estado y comprobar que los cuatro aparecen en consulta. | 
| **RF-11** | Persistir y recuperar estados | Conservar los turnos, estados, secuencias y asignaciones al cerrar normalmente y reiniciar la aplicación. | Registrar, asignar, atender y cancelar; reiniciar y comparar datos, incluyendo ventanilla ocupada. | 
| **RF-12** | Retroalimentación de operaciones | Confirmar operaciones correctas e informar claramente las rechazadas, sin indicar éxito cuando falle la persistencia necesaria. | Provocar operación inválida y fallo controlado de escritura; comprobar mensajes y resultado. | 

## 2. Reglas de Negocio Vinculantes (RN)

* **RN-01**: Todos los turnos comparten una sola cola FIFO, sin prioridades.
* **RN-02**: El orden FIFO se determina por una secuencia creciente asignada al registrar el turno, no por orden alfabético del ID.
* **RN-03**: Solo existen las ventanillas `V01`, `V02` y `V03`; están predefinidas.
* **RN-04**: Solo puede existir un turno *En atención* por ventanilla.
* **RN-05**: Un turno no puede estar *En atención* en dos ventanillas simultáneamente.
* **RN-06**: Asignar un turno implica iniciar inmediatamente su atención.
* **RN-07**: Un turno *En espera* puede cancelarse; uno *En atención*, *Atendido* o *Cancelado* **no** puede cancelarse.
* **RN-08**: Solo un turno *En atención* puede finalizarse y pasar a *Atendido*.
* **RN-09**: Los turnos *Atendido* y *Cancelado* no regresan a *En espera* ni se eliminan del historial.
* **RN-10**: Si no existen turnos pendientes, una solicitud de asignación no altera los datos.

## 3. Interfaces Externas y Requisitos de Datos (RI / RD)

### 3.1. Interfaz de Usuario (RI)
* **RI-01 (Operaciones visibles)**: La interfaz ofrecerá controles identificables para registrar turno, consultar fila, consultar ventanillas, asignar desde ventanilla, finalizar, cancelar por ID y consultar historial.
* **RI-02 (Visualización consistente)**: La interfaz actualizará claramente la fila, el estado de ventanillas y el historial después de cada operación sin mostrar confirmaciones contradictorias.
* **RI-03 (Identificación de acciones)**: Los controles usarán etiquetas en español comprensibles diferenciando claramente “Asignar siguiente”, “Finalizar” y “Cancelar”.

### 3.2. Persistencia y Datos (RD)
* **RD-01 (Almacenamiento local)**: Los datos persistentes se almacenarán localmente en archivo o base de datos, sin requerir cuentas ni conectividad externa.
* **RD-02 (Integridad de operación)**: Toda operación de asignación, finalización o cancelación dejará consistentes turnos y ventanillas; ante un fallo de guardado, no se comunicará éxito ni se dejará almacenamiento parcial.
* **RD-03 (Validación de datos persistidos)**: Si al iniciar no es posible leer la información o es inválida, el sistema informará el problema y evitará sobrescribir silenciosamente los datos existentes.

## 4. Requisitos No Funcionales (RNF)

Organizados según las características de calidad del modelo de referencia:

### 4.1. Capacidad de Interacción / Usabilidad
* **RNF-U01 (Operabilidad)**: Un usuario con conocimientos básicos de la descripción del sistema debe poder registrar, asignar, finalizar y cancelar turnos desde la interfaz sin usar comandos internos ni editar archivos.
* **RNF-U02 (Mensajes útiles)**: Ante acciones no permitidas, el sistema explicará la causa (ej. ventanilla ocupada) e indicará el identificador implicado.
* **RNF-U03 (Estado identificable)**: La interfaz distinguirá visualmente y mediante texto los cuatro estados posibles, sin depender exclusivamente de colores.
* **RNF-U04 (Prevención de errores)**: Antes de cancelar un turno pendiente, la interfaz solicitará confirmación explícita.

### 4.2. Seguridad y Protección de la Información
* **RNF-S01 (Validación en lógica)**: Las reglas de estado y asignación se aplicarán estrictamente en el dominio, aun si se invocan sin pasar por la interfaz gráfica.
* **RNF-S02 (Sin secretos embebidos)**: El código y la documentación no incluirán contraseñas, tokens o claves API.
* **RNF-S03 (Alcance local)**: La solución funcionará localmente sin exponer datos a internet, contemplando un único operador local.
* **RNF-S04 (Manejo seguro de entradas)**: Los identificadores y entradas recibidas serán validados antes de su uso para evitar fallos no controlados.

### 4.3. Mantenibilidad y Calidad de Construcción
* **RNF-M01 (Separación de responsabilidades)**: Las reglas FIFO y transiciones de estado deben verificarse independientemente de la interfaz gráfica (MVC desacoplado).
* **RNF-M02 (Estándar y legibilidad)**: El código seguirá un estándar claro, con nombres significativos y sin duplicación injustificada de reglas de negocio.
* **RNF-M03 (Pruebas automatizadas)**: Incorporar pruebas unitarias automatizadas para FIFO, estados, asignaciones y rechazos ejecutables mediante instrucciones documentadas (`mvn test`).
* **RNF-M04 (Documentación mínima)**: El repositorio contendrá instrucciones claras para preparar, ejecutar y probar la aplicación.

### 4.4. Fiabilidad y Rendimiento
* **RNF-R01 (Recuperación tras reinicio)**: Tras un cierre normal, la siguiente ejecución recuperará íntegramente las operaciones y ventanillas ocupadas.
* **RNF-R02 (Integridad ante rechazos)**: Toda operación inválida dejará completamente inalterados los datos y el orden de la fila.
* **RNF-P01 (Respuesta interactiva)**: Con hasta 200 turnos almacenados, las operaciones normales responderán en un tiempo no mayor a 2 segundos en el equipo de demostración.