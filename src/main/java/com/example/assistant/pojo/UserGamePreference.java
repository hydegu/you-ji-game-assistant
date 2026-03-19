package com.example.assistant.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGamePreference {

    /** 当前在玩的游戏 */
    private String currentGame;

    /** 游戏进度/章节 */
    private String chapter;

    /** 游戏风格：全成就 / 速通 / 剧情向 */
    private String playstyle;

    /** 偏好难度 */
    private String difficulty;

    /** 喜欢的游戏类型 */
    private String favoriteGenre;

    /**
     * 合并另一个偏好对象，非 null 字段才覆盖（保留历史数据）
     */
    public void mergeFrom(UserGamePreference newer) {
        if (newer.currentGame  != null) this.currentGame  = newer.currentGame;
        if (newer.chapter      != null) this.chapter      = newer.chapter;
        if (newer.playstyle    != null) this.playstyle    = newer.playstyle;
        if (newer.difficulty   != null) this.difficulty   = newer.difficulty;
        if (newer.favoriteGenre!= null) this.favoriteGenre= newer.favoriteGenre;
    }

    /**
     * 转成给 AI 看的自然语言提示
     */
    public String toContextPrompt() {
        StringBuilder sb = new StringBuilder("用户游戏偏好：\n");
        if (currentGame   != null) sb.append("- 当前游戏：").append(currentGame).append("\n");
        if (chapter       != null) sb.append("- 游戏进度：").append(chapter).append("\n");
        if (playstyle     != null) sb.append("- 游戏风格：").append(playstyle).append("\n");
        if (difficulty    != null) sb.append("- 偏好难度：").append(difficulty).append("\n");
        if (favoriteGenre != null) sb.append("- 喜欢类型：").append(favoriteGenre).append("\n");
        return sb.toString();
    }

    /** 判断是否有任何有效偏好 */
    public boolean isEmpty() {
        return currentGame == null && chapter == null && playstyle == null
            && difficulty == null && favoriteGenre == null;
    }
}