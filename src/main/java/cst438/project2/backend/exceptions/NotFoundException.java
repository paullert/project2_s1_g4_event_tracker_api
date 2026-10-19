package cst438.project2.backend.exceptions;


/**
 * @author Paul Conner
 * @since
 *
 **/

public class NotFoundException extends RuntimeException {
  public NotFoundException(Long id, String entityName) {
    super(String.format("%s with id %d not found", entityName, id));
  }
}
