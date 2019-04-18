#!/bin/bash
while [ $# -gt 0 ]
do
    case "$1" in
        --threshold)
            shift
            if [ $# -gt 0 ]; then
                data="$data&actionThreshold=$1"
            else
                echo "No threshold specified"
            fi
            shift
            ;;
        --discount)
            shift
            if [ $# -gt 0 ]; then
                data="$data&discountFactor=$1"
            else
                echo "No discount specified"
            fi
            shift
            ;;
        --epsilon)
            shift
            if [ $# -gt 0 ]; then
                data="$data&epsilon=$1"
            else
                echo "No epsilon specified"
            fi
            shift
            ;;
        --reward-base)
            shift
            if [ $# -gt 0 ]; then
                data="$data&baseReward=$1"
            else
                echo "No base reward specified"
            fi
            shift
            ;;
        --reward-win)
            shift
            if [ $# -gt 0 ]; then
                data="$data&winReward=$1"
            else
                echo "No win reward specified"
            fi
            shift
            ;;
        --reward-loss)
            shift
            if [ $# -gt 0 ]; then
                data="$data&lossReward=$1"
            else
                echo "No loss reward specified"
            fi
            shift
            ;;
        --reward-my-base-attack)
            shift
            if [ $# -gt 0 ]; then
                data="$data&myBaseAttackReward=$1"
            else
                echo "No 'my base attack' reward specified"
            fi
            shift
            ;;
        --reward-entity-base-attack)
            shift
            if [ $# -gt 0 ]; then
                data="$data&enemyBaseAttackReward=$1"
            else
                echo "No 'enemy base attack' reward specified"
            fi
            shift
            ;;
        --reward-my-entity-kill)
            shift
            if [ $# -gt 0 ]; then
                data="$data&myEntityKillReward=$1"
            else
                echo "No 'my entity kill' reward specified"
            fi
            shift
            ;;
        --reward-enemy-entity-kill)
            shift
            if [ $# -gt 0 ]; then
                data="$data&enemyEntityKillReward=$1"
            else
                echo "No 'enemy entity kill' reward specified"
            fi
            shift
            ;;
        --reward-entity-recruit)
            shift
            if [ $# -gt 0 ]; then
                data="$data&entityRecruitReward=$1"
            else
                echo "No entity recruit reward specified"
            fi
            shift
            ;;
        --reward-gold-find)
            shift
            if [ $# -gt 0 ]; then
                data="$data&goldFindReward=$1"
            else
                echo "No gold find reward specified"
            fi
            shift
            ;;
        --samples)
            shift
            if [ $# -gt 0 ]; then
                data="$data&learningSamplesLimit=$1"
            else
                echo "No samples specified"
            fi
            shift
            ;;
        --bots-dir)
            shift
            if [ $# -gt 0 ]; then
                data="$data&botsDirectory=$1"
            else
                echo "No bots-dir specified"
            fi
            shift
            ;;
        --samples-dir)
            shift
            if [ $# -gt 0 ]; then
                data="$data&samplesDirectory=$1"
            else
                echo "No samples-dir specified"
            fi
            shift
            ;;
        --duplication)
            shift
            if [ $# -gt 0 ]; then
                data="$data&allowBotDuplication=$1"
            else
                echo "No duplication specified"
            fi
            shift
            ;;
        --bind1)
            shift
            if [ $# -gt 0 ]; then
                data="$data&botBindingForPlayer1=$1"
            else
                echo "No bind1 specified"
            fi
            shift
            ;;
        --bind2)
            shift
            if [ $# -gt 0 ]; then
                data="$data&botBindingForPlayer2=$1"
            else
                echo "No bind2 specified"
            fi
            shift
            ;;
        *)
            break
            ;;
    esac
done

curl -s -X POST -d "$data" http://localhost:4321/admin/params
