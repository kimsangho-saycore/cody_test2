package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name GitlabPrjCreator
 * @date 2023-10-25
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-25        zeroj0706       최초 생성
 **/

@Data
public class GitlabPrjCreator {
    /* 페이지 정보 */
    private String page;
    /* 출력 수 */
    private String size;
    /* 검색 조건 (projectName : 프로젝트명, projectId : 프로젝트 ID, projectURL : 프로젝트 URL) */
    private String searchType;
    /* 검색 단어 */
    private String searchText;
    /* GITLAB 아이디 */
    private String userName;
    private String evLang;
}
