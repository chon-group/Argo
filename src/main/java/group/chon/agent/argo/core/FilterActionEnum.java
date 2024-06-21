package group.chon.agent.argo.core;

public enum FilterActionEnum {

    EXCEPT, // Filtra todas as percepções com exceção das especificadas.
    ONLY, // Filtra somente as percepções especificadas.
    VALUE, // Filtra uma percepção por valor usando uma expressão.
    REMOVE; // Remove qualquer filtro ativo.


    public static FilterActionEnum getFilterAction(String filterActionName) {
        for (FilterActionEnum filterActionEnum : values()) {
            if(filterActionEnum.name().equalsIgnoreCase(filterActionName)) {
                return  filterActionEnum;
            }
        }
        return null;
    }
}
