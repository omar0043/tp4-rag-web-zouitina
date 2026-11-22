package ma.emsi.zouitina.tp4ragwebzouitina.jsf;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BbRoleTest {
    @Test
    void lesValeursDuMenuRestentValidesApresNormalisationDuFormulaire() {
        var bean = new Bb();
        assertEquals("documentaire", bean.getRoleSysteme());
        assertEquals(List.of("documentaire", "traducteur", "guide"),
                bean.getRolesSysteme().stream().map(item -> item.getValue().toString()).toList());
        for (var item : bean.getRolesSysteme()) {
            String id = item.getValue().toString();
            assertFalse(id.contains("\n"));
            assertFalse(id.contains("\r"));
            assertFalse(Bb.texteRole(id).isBlank());
        }
        assertThrows(IllegalArgumentException.class, () -> Bb.texteRole("inconnu"));
    }
}
