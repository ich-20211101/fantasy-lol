export const PLAYER_SCORE_FORMULA_TERMS = [
  'kills × 3',
  '+ assists × 1',
  '− deaths × 1',
  '+ win_bonus × 5',
  '+ cs × 0.01',
  '+ damage_to_champions × 0.001',
  '+ vision_score × 0.2',
]

export const PLAYER_SCORE_FORMULA = PLAYER_SCORE_FORMULA_TERMS.join(' ')
