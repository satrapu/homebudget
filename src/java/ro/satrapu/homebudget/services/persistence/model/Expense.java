package ro.satrapu.homebudget.services.persistence.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author satrapu
 */
@Entity
@Table(name = "EXPENSES")
@Data
@EqualsAndHashCode(callSuper = true)
public class Expense extends MoneyAmount {
}